/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yhml.restful.service;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.zhaow.restful.common.ServiceHelper;
import com.zhaow.restful.navigator.RestServiceProject;

import java.util.List;

public class RestProjectService {
    private Project myProject;

    public RestProjectService(Project myProject) {
        this.myProject = myProject;
    }

    public static RestProjectService getInstance(Project project) {
        return ServiceManager.getService(project, RestProjectService.class);
    }

    public List<RestServiceProject> getServiceProjects() {
        List<RestServiceProject> list = DumbService.getInstance(myProject).runReadActionInSmartMode(() -> ServiceHelper.buildRestServiceProjectListUsingResolver(myProject));
        return list;
    }
}
