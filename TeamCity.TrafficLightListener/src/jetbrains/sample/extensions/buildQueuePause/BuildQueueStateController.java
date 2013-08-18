/*
 * Copyright 2000-2013 JetBrains s.r.o.
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

package jetbrains.sample.extensions.buildQueuePause;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jetbrains.buildServer.controllers.BaseActionController;
import jetbrains.buildServer.serverSide.CriticalErrors;
import jetbrains.buildServer.util.PropertiesUtil;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.openapi.ControllerAction;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildServer.web.util.SessionUser;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * @author Yegor.Yarko
 *         Date: 10.03.2009
 */

public class BuildQueueStateController extends BaseActionController {
  private final StartBuildPrecondition myStartBuildPrecondition;
  private static final String BUILD_QUEUE_PAUSE_PARAMETER_NAME = "newBuildQueuePausedState";
  @NonNls public static final String QUEUE_IS_PAUSED = "buildQueueIsPaused";
  private final CriticalErrors myCriticalErrors;

  public BuildQueueStateController(final WebControllerManager manager,
                                   StartBuildPrecondition startBuildPrecondition,
                                   final CriticalErrors criticalErrors) {
    super(manager);
    myCriticalErrors = criticalErrors;

    manager.registerController("/queuePauser.html", this);

    myStartBuildPrecondition = startBuildPrecondition;
    init();
  }

  private void init() {
    registerAction(new ControllerAction() {
      public boolean canProcess(@NotNull final HttpServletRequest request) {
        return request.getParameter(BUILD_QUEUE_PAUSE_PARAMETER_NAME) != null;
      }

      public void process(@NotNull final HttpServletRequest request, @NotNull final HttpServletResponse response, @Nullable final Element ajaxResponse) {
        boolean newPausedState = PropertiesUtil.getBoolean(request.getParameter(BUILD_QUEUE_PAUSE_PARAMETER_NAME));
        myStartBuildPrecondition.setQueuePaused(newPausedState, SessionUser.getUser(request));
        if (newPausedState){
          myCriticalErrors.putError(QUEUE_IS_PAUSED, "Build Queue is paused");
        } else{
          myCriticalErrors.clearError(QUEUE_IS_PAUSED);
        }
      }
    });
  }

  @Override
  protected ModelAndView doHandle(@NotNull final HttpServletRequest request, @NotNull final HttpServletResponse response) throws Exception {
    doAction(request, response, null);

    String redirectTo = request.getParameter("redirectTo");
    if (StringUtil.isEmpty(redirectTo)) {
      redirectTo = request.getHeader("Referer");
      if (redirectTo == null) return null;
    }

    return new ModelAndView(new RedirectView(redirectTo));
  }
}
