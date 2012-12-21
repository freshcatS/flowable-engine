/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.bpmn.converter.parser;

import javax.xml.stream.XMLStreamReader;

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.model.BpmnModel;
import org.apache.commons.lang.StringUtils;

/**
 * @author Tijs Rademakers
 */
public class MessageParser implements BpmnXMLConstants {
  
  public void parse(XMLStreamReader xtr, BpmnModel model) throws Exception {
    if (StringUtils.isNotEmpty(xtr.getAttributeValue(null, ATTRIBUTE_ID))) {
      String messageId = model.getTargetNamespace() + ":" + xtr.getAttributeValue(null, ATTRIBUTE_ID);
      String messageName = xtr.getAttributeValue(null, ATTRIBUTE_NAME);
      String itemRef = parseItemRef(xtr.getAttributeValue(null, ATTRIBUTE_ITEM_REF), model);
      model.addMessage(messageId, messageName, itemRef);
    }
  }
  
  protected String parseItemRef(String itemRef, BpmnModel model) {
    String result = null;
    if (StringUtils.isNotEmpty(itemRef)) {
      int indexOfP = itemRef.indexOf(':');
      if (indexOfP != -1) {
        String prefix = itemRef.substring(0, indexOfP);
        String resolvedNamespace = model.getNamespace(prefix);
        result = resolvedNamespace + ":" + itemRef.substring(indexOfP + 1);
      } else {
        result = model.getTargetNamespace() + ":" + itemRef;
      }
    }
    return result;
  }
}
