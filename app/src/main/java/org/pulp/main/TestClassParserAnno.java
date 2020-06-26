package org.pulp.main;

import org.json.JSONException;
import org.json.JSONObject;
import org.pulp.fastapi.i.InterpreterParseBefore;
import org.pulp.fastapi.i.InterpreterParseError;
import org.pulp.fastapi.i.InterpreterParserAfter;
import org.pulp.fastapi.i.InterpreterParserCustom;
import org.pulp.fastapi.model.Error;
import org.pulp.fastapi.model.IModel;
import org.pulp.fastapi.util.Log;

public class TestClassParserAnno implements InterpreterParserCustom, InterpreterParseError, InterpreterParseBefore, InterpreterParserAfter<IModel> {
    @Override
    public String onBeforeParse(String json) {
        Log.out("TestClassParserAnno.onBeforeParse");
        try {
            JSONObject jsonObject = new JSONObject(json);
            jsonObject.put("testFrom", "class onBeforeParse");
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    @Override
    public Error onParseError(String json) {
        Log.out("TestClassParserAnno.onParseError");
        Error error = new Error();
        error.setCode(888);
        error.setMsg("class onParseError");
        return error;
    }

    @Override
    public Object onCustomParse(String json) {
        Log.out("TestClassParserAnno.onCustomParse");
        TestModel testModel = new TestModel();
        testModel.testFrom = "class onCustomParse";
        return testModel;
    }


    @Override
    public void onParseCompleted(IModel bean) {
        Log.out("TestClassParserAnno.onParseCompleted:" + bean);
    }
}
