package es.uvigo.ei.sing.singulator.json;

import java.io.Serializable;

public class JsonML implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean ml;
    private String mlOutput;
    private String python;
    private Integer jump;
    private Integer ts;
    private String option;
    private int division;
    private String json;
    private String model;
    private String target;
    private String data;


    public JsonML() {
    }

    public boolean isMl() {
        return ml;
    }

    public void setMl(boolean ml) {
        this.ml = ml;
    }

    public String getMlOutput() {
        return mlOutput;
    }

    public void setMlOutput(String mlOutput) {
        this.mlOutput = mlOutput;
    }

    public Integer getJump() {
        return jump;
    }

    public void setJump(Integer jump) {
        this.jump = jump;
    }


    public int getTs() {
        return ts;
    }

    public void setTs(int ts) {
        this.ts = ts;
    }

    public String getPython() {
        return python;
    }
    public void setPython(String python) {
        this.python = python;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public int getDivision() {
        return division;
    }

    public void setDivision(int division) {
        this.division = division;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getData() {
        return data;
    }
    public void setData(String data) {
        this.data = data;
    }

}
