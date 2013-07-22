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
package de.perdian.downloader.ui.swing.resources;

import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * Placeholder and factory for obtaining icons
 *
 * @author Christian Robert
 */

public class Icons {

  public static ImageIcon createIcon(String iconName) {
    URL resourceURL = Icons.class.getResource(iconName);
    if(resourceURL == null) {
      throw new IllegalArgumentException("No icon resource available at: " + iconName);
    } else {
      try {
        return new ImageIcon(ImageIO.read(resourceURL));
      } catch(IOException e) {
        throw new IllegalArgumentException("Cannot load image resource from: " + resourceURL, e);
      }
    }
  }

}