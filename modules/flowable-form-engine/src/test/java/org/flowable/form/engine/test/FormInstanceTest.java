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
package org.flowable.form.engine.test;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.flowable.form.api.FormInfo;
import org.flowable.form.api.FormInstance;
import org.flowable.form.api.FormInstanceInfo;
import org.flowable.form.model.FormField;
import org.flowable.form.model.SimpleFormModel;
import org.joda.time.LocalDate;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

public class FormInstanceTest extends AbstractFlowableFormTest {

    @Test
    @FormDeploymentAnnotation(resources = "org/flowable/form/engine/test/deployment/simple.form")
    public void submitSimpleForm() throws Exception {
        FormInfo formInfo = repositoryService.getFormModelByKey("form1");

        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put("input1", "test");
        Map<String, Object> formValues = formService.getVariablesFromFormSubmission(formInfo, valuesMap, "default");
        assertEquals("test", formValues.get("input1"));

        FormInstance formInstance = formService.createFormInstance(formValues, formInfo, null, null, null);
        assertEquals(formInfo.getId(), formInstance.getFormDefinitionId());
        JsonNode formNode = formEngineConfiguration.getObjectMapper().readTree(formInstance.getFormValueBytes());
        assertEquals("test", formNode.get("values").get("input1").asText());
        assertEquals("default", formNode.get("flowable_form_outcome").asText());

        FormInstanceInfo formInstanceModel = formService.getFormInstanceModelById(formInstance.getId(), null);
        assertEquals("form1", formInstanceModel.getKey());
        
        SimpleFormModel formModel = (SimpleFormModel) formInstanceModel.getFormModel();
        assertEquals(1, formModel.getFields().size());
        FormField formField = formModel.getFields().get(0);
        assertEquals("input1", formField.getId());
        assertEquals("test", formField.getValue());
    }

    @Test
    @FormDeploymentAnnotation(resources = "org/flowable/form/engine/test/deployment/form_with_dates.form")
    public void submitDateForm() throws Exception {
        FormInfo formInfo = repositoryService.getFormModelByKey("dateform");

        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put("input1", "test");
        valuesMap.put("date1", "2016-01-01");
        valuesMap.put("date2", "2017-01-01");
        Map<String, Object> formValues = formService.getVariablesFromFormSubmission(formInfo, valuesMap, "date");
        assertEquals("test", formValues.get("input1"));
        assertEquals(new LocalDate(2016, 1, 1), formValues.get("date1"));
        assertEquals(new LocalDate(2017, 1, 1), formValues.get("date2"));

        FormInstance formInstance = formService.createFormInstance(formValues, formInfo, null, null, null);
        assertEquals(formInfo.getId(), formInstance.getFormDefinitionId());
        JsonNode formNode = formEngineConfiguration.getObjectMapper().readTree(formInstance.getFormValueBytes());
        JsonNode valuesNode = formNode.get("values");
        assertEquals(3, valuesNode.size());
        assertEquals("test", valuesNode.get("input1").asText());
        assertEquals("2016-01-01", valuesNode.get("date1").asText());
        assertEquals("2017-01-01", valuesNode.get("date2").asText());
        assertEquals("date", formNode.get("flowable_form_outcome").asText());
    }

    @Test
    @FormDeploymentAnnotation(resources = "org/flowable/form/engine/test/deployment/simple.form")
    public void saveSimpleForm() throws Exception {
        String taskId = "123456";
        FormInfo formInfo = repositoryService.getFormModelByKey("form1");

        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put("input1", "test");
        Map<String, Object> formValues = formService.getVariablesFromFormSubmission(formInfo, valuesMap, "default");

        FormInstance formInstance = formService.saveFormInstance(formValues, formInfo, taskId, null, null);
        assertEquals(formInfo.getId(), formInstance.getFormDefinitionId());
        JsonNode formNode = formEngineConfiguration.getObjectMapper().readTree(formInstance.getFormValueBytes());
        assertEquals("test", formNode.get("values").get("input1").asText());
        assertEquals("default", formNode.get("flowable_form_outcome").asText());

        FormInstanceInfo formInstanceModel = formService.getFormInstanceModelById(formInstance.getId(), null);
        assertEquals("form1", formInstanceModel.getKey());
        SimpleFormModel formModel = (SimpleFormModel) formInstanceModel.getFormModel();
        assertEquals(1, formModel.getFields().size());
        FormField formField = formModel.getFields().get(0);
        assertEquals("input1", formField.getId());
        assertEquals("test", formField.getValue());

        valuesMap = new HashMap<>();
        valuesMap.put("input1", "updatedValue");
        formValues = formService.getVariablesFromFormSubmission(formInfo, valuesMap, "updatedOutcome");

        formInstance = formService.saveFormInstance(formValues, formInfo, taskId, null, null);
        assertEquals(formInfo.getId(), formInstance.getFormDefinitionId());
        formNode = formEngineConfiguration.getObjectMapper().readTree(formInstance.getFormValueBytes());
        assertEquals("updatedValue", formNode.get("values").get("input1").asText());
        assertEquals("updatedOutcome", formNode.get("flowable_form_outcome").asText());

        formInstanceModel = formService.getFormInstanceModelById(formInstance.getId(), null);
        assertEquals("form1", formInstanceModel.getKey());
        formModel = (SimpleFormModel) formInstanceModel.getFormModel();
        assertEquals(1, formModel.getFields().size());
        formField = formModel.getFields().get(0);
        assertEquals("input1", formField.getId());
        assertEquals("updatedValue", formField.getValue());

        assertEquals(1, formService.createFormInstanceQuery().formDefinitionId(formInfo.getId()).count());
    }
}
