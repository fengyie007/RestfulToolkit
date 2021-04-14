package com.zhaow.restful.navigator;


import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.treeStructure.*;
import com.intellij.util.OpenSourceUtil;
import com.yhml.restful.service.RestProjectService;
import com.zhaow.restful.common.KtFunctionHelper;
import com.zhaow.restful.common.PsiMethodHelper;
import com.zhaow.restful.common.ToolkitIcons;
import com.zhaow.restful.method.HttpMethod;
import com.zhaow.restful.navigation.action.RestServiceItem;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.idea.KotlinLanguage;
import org.jetbrains.kotlin.psi.KtNamedFunction;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gnu.trove.THashMap;

public class RestServiceStructure extends SimpleTreeStructure {
    public static final Logger LOG = Logger.getInstance(RestServiceStructure.class);
    private final Project myProject;
    private final Map<RestServiceProject, ProjectNode> myProjectToNodeMapping = new THashMap<>();
    RestServiceDetail myRestServiceDetail;
    // fixme: 2020.3 以后不兼容
    private SimpleTreeBuilder myTreeBuilder;
    private SimpleTree myTree;

    private RootNode myRoot = new RootNode();
    private int serviceCount = 0;

    public RestServiceStructure(Project project, SimpleTree tree) {
        myProject = project;
        myTree = tree;
        myRestServiceDetail = project.getComponent(RestServiceDetail.class);

        tree.setRootVisible(true);
        tree.setShowsRootHandles(true);

        // fixme: 2020.3 以后不兼容
        myTreeBuilder = new SimpleTreeBuilder(tree, (DefaultTreeModel) tree.getModel(), this, null);

        // fixme: 2020.3 以后不兼容
        myTreeBuilder.initRoot();

        // fixme: 2020.3 以后不兼容
        myTreeBuilder.expand(myRoot, null);
        Disposer.register(myProject, myTreeBuilder);
    }

    public static <T extends BaseSimpleNode> List<T> getSelectedNodes(SimpleTree tree, Class<T> nodeClass) {
        final List<T> filtered = new ArrayList<>();
        for (SimpleNode node : getSelectedNodes(tree)) {
            if ((nodeClass != null) && (!nodeClass.isInstance(node))) {
                filtered.clear();
                break;
            }
            filtered.add((T) node);
        }
        return filtered;
    }

    private static List<SimpleNode> getSelectedNodes(SimpleTree tree) {
        List<SimpleNode> nodes = new ArrayList<>();
        TreePath[] treePaths = tree.getSelectionPaths();
        if (treePaths != null) {
            for (TreePath treePath : treePaths) {
                nodes.add(tree.getNodeFor(treePath));
            }
        }
        return nodes;
    }

    @Override
    public RootNode getRootElement() {
        return myRoot;
    }

    public void update() {
        //        myTreeBuilder.setClearOnHideDelay(4);
        //        myTreeBuilder.cleanUp();
        //        myTreeBuilder.initRoot();
        //        myTreeBuilder.expand(myRoot, null);
        // rest service controller
/*
        SimpleTree tree1 = new SimpleTree();
        myRoot = new RootNode();
        myTreeBuilder = new SimpleTreeBuilder(tree1, (DefaultTreeModel)tree1.getModel(), this, null);
        Disposer.register(myProject, myTreeBuilder);

        myTreeBuilder.initRoot();
        myTreeBuilder.expand(myRoot, null);*/
        //        List<RestServiceProject> projects = myProjectsManager.getProjects();
        //        List<RestServiceProject> projects = RestServiceProjectsManager.getInstance(myProject).getProjects();
/*        if (!ModalityState.current().equals(ModalityState.any()) && myProject.isInitialized()) {
            PsiDocumentManager.getInstance(myProject).commitAllDocuments();
        }*/

        List<RestServiceProject> projects = RestProjectService.getInstance(myProject).getServiceProjects();


        //        Set<RestServiceProject> deleted = new HashSet<>(myProjectToNodeMapping.keySet());
        //        deleted.removeAll(projects);
        updateProjects(projects);
    }

    public void updateProjects(List<RestServiceProject> projects) {
        serviceCount = 0;
        //        DefaultMutableTreeNode rootTreeNode = createTreeNode("REST Services");
        //        myTreeBuilder.addSubtreeToUpdate(rootTreeNode);

        for (RestServiceProject each : projects) {
            serviceCount += each.serviceItems.size();
            ProjectNode node = findNodeFor(each);
            if (node == null) {
                node = new ProjectNode(myRoot, each);
                myProjectToNodeMapping.put(each, node);
            }
        }
        //        fixme: 2020.3 以后不兼容
        myTreeBuilder.getUi().doUpdateFromRoot();
        //        ((CachingSimpleNode) myRoot.getParent()).cleanUpCache();
        //        myRoot.childrenChanged();
        myRoot.updateProjectNodes(projects);
    }

    private ProjectNode findNodeFor(RestServiceProject project) {
        return myProjectToNodeMapping.get(project);
    }

    public void updateFrom(SimpleNode node) {
        //        fixme: 2020.3 以后不兼容
        if (node != null)
            myTreeBuilder.addSubtreeToUpdateByElement(node);
    }

    private void updateUpTo(SimpleNode node) {
        SimpleNode each = node;
        while (each != null) {
            SimpleNode parent = each.getParent();
            /*if (parent != null) {
                ((BaseSimpleNode)parent).cleanUpCache();
            }*/
            updateFrom(each);
            each = each.getParent();
        }
    }

    private void resetRestServiceDetail() {
        myRestServiceDetail.resetRequestTabbedPane();
        myRestServiceDetail.setMethodValue(HttpMethod.GET.name());
        myRestServiceDetail.setUrlValue("URL");
        myRestServiceDetail.initTab();
    }

    public abstract class BaseSimpleNode extends CachingSimpleNode {

        protected BaseSimpleNode(SimpleNode aParent) {
            super(aParent);
        }

        @Nullable
        @NonNls
        String getActionId() {
            return null;
        }

        @Nullable
        @NonNls
        String getMenuId() {
            return null;
        }

        @Override
        public void cleanUpCache() {
            super.cleanUpCache();
        }

        protected void childrenChanged() {
            BaseSimpleNode each = this;
            while (each != null) {
                each.cleanUpCache();
                each = (BaseSimpleNode) each.getParent();
            }
            updateUpTo(this);
        }

    }

    public class RootNode extends BaseSimpleNode {
        List<ProjectNode> projectNodes = new ArrayList<>();

        protected RootNode() {
            super(null);
            //            getTemplatePresentation().setIcon(AllIcons.Actions.Module);
            //            setIcon(AllIcons.Actions.Module); //兼容 IDEA 2016
        }

        @Override
        protected SimpleNode[] buildChildren() {
            return projectNodes.toArray(new SimpleNode[0]);
        }

        @Override
        public String getName() {
            String s = "Found %d services ";// in {controllerCount} Controllers";
            return serviceCount > 0 ? String.format(s, serviceCount) : null;
        }

        @Override
        public void handleSelection(SimpleTree tree) {
            //            System.out.println("ProjectNode handleSelection");
            resetRestServiceDetail();

        }

        public void updateProjectNodes(List<RestServiceProject> projects) {
            //            cleanUpCache();
            projectNodes.clear();
            for (RestServiceProject project : projects) {
                ProjectNode projectNode = new ProjectNode(this, project);
                projectNodes.add(projectNode);
            }

            //                projectNode.updateServiceNodes();

            /*SimpleNode parent = getParent();
            if (parent != null) {
                ((BaseSimpleNode)parent).cleanUpCache();
            }*/
            updateFrom(getParent());
            childrenChanged();
            //            updateUpTo(this);

        }

    }

    public class ProjectNode extends BaseSimpleNode {
        List<ServiceNode> serviceNodes = new ArrayList<>();
        RestServiceProject myProject;


        public ProjectNode(SimpleNode parent,/*,List<RestServiceItem> serviceItems*/RestServiceProject project) {
            //            super(parent);
            super(parent);
            myProject = project;

            //            getTemplatePresentation().setIcon(ToolkitIcons.MODULE);
            //            setIcon(ToolkitIcons.MODULE); //兼容 IDEA 2016

            updateServiceNodes(project.serviceItems);
        }

        private void updateServiceNodes(List<RestServiceItem> serviceItems) {
            serviceNodes.clear();
            for (RestServiceItem serviceItem : serviceItems) {
                serviceNodes.add(new ServiceNode(this, serviceItem));
            }
            SimpleNode parent = getParent();
            if (parent != null) {
                ((BaseSimpleNode) parent).cleanUpCache();
            }
            updateFrom(parent);
            //            childrenChanged();
            //            updateUpTo(this);
        }

        @Override
        protected SimpleNode[] buildChildren() {
            return serviceNodes.toArray(new SimpleNode[0]);
        }

        @Override
        public String getName() {
            return myProject.getModuleName();
        }


        @Override
        @Nullable
        @NonNls
        protected String getActionId() {
            return "Toolkit.RefreshServices";
        }
/*
        @Override
        @Nullable
        @NonNls
        protected String getMenuId() {
            return "Toolkit.ReimportServices";
        }*/

        @Override
        public void handleSelection(SimpleTree tree) {
            //            System.out.println("ProjectNode handleSelection");
            resetRestServiceDetail();
        }

        @Override
        public void handleDoubleClickOrEnter(SimpleTree tree, InputEvent inputEvent) {
            //            System.out.println("ProjectNode handleDoubleClickOrEnter");
        }
    }

    public class ServiceNode extends BaseSimpleNode {
        RestServiceItem myServiceItem;

        public ServiceNode(SimpleNode parent, RestServiceItem serviceItem) {
            super(parent);
            myServiceItem = serviceItem;

            Icon icon = ToolkitIcons.METHOD.get(serviceItem.getMethod());
            if (icon != null) {
                getTemplatePresentation().setIcon(icon);
                setIcon(icon); //兼容 IDEA 2016
            }
        }

        @Override
        protected SimpleNode[] buildChildren() {
            return new SimpleNode[0];
        }

        @Override
        public String getName() {
            String name = myServiceItem.getName();
/*            if (ToolkitIcons.METHOD.get(myServiceItem.getMethod()) == null && myServiceItem.getMethod() != null) {
                name += " [" + myServiceItem.getMethod() + "]";
            }*/
            return name;
        }

        @Override
        public void handleSelection(SimpleTree tree) {
            ServiceNode selectedNode = (ServiceNode) tree.getSelectedNode();
            showServiceDetail(selectedNode.myServiceItem);
        }

        @Override
        public void handleDoubleClickOrEnter(SimpleTree tree, InputEvent inputEvent) {
            ServiceNode selectedNode = (ServiceNode) tree.getSelectedNode();

            RestServiceItem myServiceItem = selectedNode.myServiceItem;
            PsiElement psiElement = myServiceItem.getPsiElement();

            if (!psiElement.isValid()) {
                // PsiDocumentManager.getInstance(psiMethod.getProject()).commitAllDocuments();
                // try refresh service
                LOG.info("psiMethod is invalid: " + psiElement.toString());
                RestServicesNavigator.getInstance(myServiceItem.getModule().getProject()).scheduleStructureUpdate();
            }

            if (psiElement.getLanguage() == JavaLanguage.INSTANCE) {
                PsiMethod psiMethod = myServiceItem.getPsiMethod();
                OpenSourceUtil.navigate(psiMethod);

            } else if (psiElement.getLanguage() == KotlinLanguage.INSTANCE) {
                if (psiElement instanceof KtNamedFunction) {
                    KtNamedFunction ktNamedFunction = (KtNamedFunction) psiElement;
                    OpenSourceUtil.navigate(ktNamedFunction);
                }
            }
        }

        /**
         * 显示服务详情，url
         */
        private void showServiceDetail(RestServiceItem serviceItem) {
            String requestParams = "";
            String requestBodyJson = "";
            String method = serviceItem.getMethod() != null ? String.valueOf(serviceItem.getMethod()) : HttpMethod.GET.name();

            myRestServiceDetail.resetRequestTabbedPane();
            myRestServiceDetail.setMethodValue(method);
            myRestServiceDetail.setUrlValue(serviceItem.getFullUrl());

            PsiElement psiElement = serviceItem.getPsiElement();

            PsiMethodHelper psiMethodHelper = null;
            if (psiElement.getLanguage() == JavaLanguage.INSTANCE) {
                psiMethodHelper = PsiMethodHelper.create(serviceItem.getPsiMethod()).withModule(serviceItem.getModule());
            } else if (psiElement.getLanguage() == KotlinLanguage.INSTANCE) {
                if (psiElement instanceof KtNamedFunction) {
                    KtNamedFunction ktNamedFunction = (KtNamedFunction) psiElement;
                    psiMethodHelper = KtFunctionHelper.create(ktNamedFunction).withModule(serviceItem.getModule());
                }
            }

            if (psiMethodHelper != null) {
                requestParams = psiMethodHelper.buildParamString();
                requestBodyJson = psiMethodHelper.buildRequestBodyJson();
            }


            if (StringUtils.isNotBlank(requestBodyJson)) {
                myRestServiceDetail.addRequestBodyTabPanel(requestBodyJson);
            } else {
                myRestServiceDetail.addRequestParamsTab(requestParams);
            }
        }

        @Override
        @Nullable
        @NonNls
        protected String getMenuId() {
            return "Toolkit.NavigatorServiceMenu";
        }

/*
        @Override
        @Nullable
        @NonNls
        protected String getActionId() {
            return "Toolkit.Navigator";
        }*/

    }

}
