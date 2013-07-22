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
package de.perdian.downloader.ui.swing;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JTextField;

/**
 * Global helpermethods
 *
 * @author Christian Robert
 */

public class DownloadGuiHelper {

  public static <T extends Component> T lookupParent(Class<T> expectedComponentClass, Component source) {
    Component currentComponent = source.getParent();
    while(currentComponent != null) {
      if(expectedComponentClass.isInstance(currentComponent)) {
        return expectedComponentClass.cast(currentComponent);
      } else {
        currentComponent = currentComponent.getParent();
      }
    }
    throw new IllegalArgumentException("Cannot find parent component of class '" + expectedComponentClass + "' in component: " + source);
  }

  public static JTextField createLabelField(String content) {
    JTextField textLabel = new JTextField();
    textLabel.setBorder(null);
    textLabel.setEditable(false);
    textLabel.setOpaque(false);
    textLabel.setText(content);
    textLabel.setPreferredSize(new Dimension(1, textLabel.getPreferredSize().height));
    textLabel.setFocusable(false);
    textLabel.setToolTipText(content);
    return textLabel;
  }

}