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

public class DownloadTools {

  public static String safeFileName(CharSequence source) {
    StringBuilder result = new StringBuilder(source.length());
    for(char c : source.toString().toCharArray()) {
      if(Character.isWhitespace(c)) {
        result.append(" ");
      } else if(Character.isLetterOrDigit(c)) {
        result.append(c);
      } else if("[]()$§!+'-_.,".indexOf(c) > -1) {
        result.append(c);
      } else {
        result.append("_");
      }
    }
    return result.toString().trim();
  }

  public static String safeUrl(String sourceUrlValue) {
    StringBuilder safeUrl = new StringBuilder();
    for(char c : sourceUrlValue.toCharArray()) {
      if(Character.isWhitespace(c)) {
        safeUrl.append("%20");
      } else {
        safeUrl.append(c);
      }
    }
    return safeUrl.toString();
  }

}