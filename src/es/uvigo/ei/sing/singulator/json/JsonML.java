package es.uvigo.ei.sing.singulator.json;

import java.io.Serializable;

public class JsonML implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean ml;
    private String mlOutput;
    private Integer jump;
    private Parameter parameter;
    private String python;

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

    public Parameter getParameter() {
        return parameter;
    }

    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    public Integer getJump() {
        return jump;
    }

    public void setJump(Integer jump) {
        this.jump = jump;
    }

    public String getPython() {
        return python;
    }
    public void setPython(String python) {
        this.python = python;
    }

    public static class Parameter implements Serializable {

        private static final long serialVersionUID = 1L;

        private String option;
        private String csv;
        private int division;
        private int ts;
        private String tg;
        private String json;
        private String model;
        private String results;

        public Parameter() {
        }

        public String getOption() {
            return option;
        }

        public void setOption(String option) {
            this.option = option;
        }

        public String getCsv() {
            return csv;
        }

        public void setCsv(String csv) {
            this.csv = csv;
        }

        public int getDivision() {
            return division;
        }

        public void setDivision(int division) {
            this.division = division;
        }

        public int getTs() {
            return ts;
        }

        public void setTs(int ts) {
            this.ts = ts;
        }

        public String getTg() {
            return tg;
        }

        public void setTg(String tg) {
            this.tg = tg;
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
        public String getResults() {
            return results;
        }

        public void setResults(String results) {
            this.results = results;
        }
    }
}
