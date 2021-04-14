package com.yhml.restful.view;

import com.intellij.ui.ColoredTreeCellRenderer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.TreeModel;

/**
 * @author Jfeng
 * @date 2021/4/14
 */
public class RestfulTreeCellRenderer extends ColoredTreeCellRenderer {

    private static final long serialVersionUID = 662344751218389340L;

    @Override
    public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        TreeModel model = tree.getModel();
    }
}
