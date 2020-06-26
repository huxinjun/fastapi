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

public class TestMethodParserAnno implements InterpreterParserCustom<TestModel>, InterpreterParseError, InterpreterParseBefore, InterpreterParserAfter<String> {
    @Override
    public String onBeforeParse(String json) {
        Log.out("TestMethodParserAnno.onBeforeParse");
        try {
            JSONObject jsonObject = new JSONObject(json);
            jsonObject.put("testFrom", "method onBeforeParse");
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }

    @Override
    public Error onParseError(String json) {
        Log.out("TestMethodParserAnno.onParseError");
        Error error = new Error();
        error.setCode(888);
        error.setMsg("method onParseError");
        return error;
    }

    @Override
    public TestModel onCustomParse(String json) {
        Log.out("TestMethodParserAnno.onCustomParse");
        TestModel testModel = new TestModel();
        testModel.testFrom = "method onCustomParse";
        return testModel;
    }

    @Override
    public void onParseCompleted(String bean) {
        Log.out("TestMethodParserAnno.onParseCompleted:" + bean);
    }
}
