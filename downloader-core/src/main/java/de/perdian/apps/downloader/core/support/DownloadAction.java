/*
 * Copyright 2013 Christian Robert
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
package de.perdian.apps.downloader.core.support;

import de.perdian.apps.downloader.core.DownloadEngine;

/**
 * Action that is executed during the launch process of the
 * {@link DownloaderLauncher}. When actions are added to a
 * {@link DownloaderLauncher} then all actions must be executed completely
 * before the {@code launch} method terminates.
 *
 * @author Christian Robert
 */

public interface DownloadAction {

  /**
   * Executes the action within the context of the given engine
   */
  public void execute(DownloadEngine engine) throws Exception;

}