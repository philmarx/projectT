package com.yywl.projectT.bean;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Key on 2017/4/24 11:20
 * email: MrKey.K@gmail.com
 * description: 阿里云OSS的用户读写Policy，bean类
 */
public class OssPolicyBean {

    /**
     * Statement : [{"Action":["oss:PutObject","oss:DeleteObject"],"Effect":"Allow","Resource":["acs:oss:*:*:samplebucket","acs:oss:*:*:samplebucket/*"]},{"Action":["oss:GetObject"],"Effect":"Allow","Resource":"acs:oss:*:1200687039332836:projectt*"}]
     * Version : 1
     */

    private String Version;
    private List<StatementBean> Statement=new LinkedList<>();

	public String getVersion() {
        return Version;
    }

    public void setVersion(String Version) {
        this.Version = Version;
    }

    public List<StatementBean> getStatement() {
        return Statement;
    }

    public void setStatement(List<StatementBean> Statement) {
        this.Statement = Statement;
    }

    public static class StatementBean {
        /**
         * Action : ["oss:PutObject","oss:DeleteObject"]
         * Effect : Allow
         * Resource : ["acs:oss:*:*:samplebucket","acs:oss:*:*:samplebucket/*"]
         */

        private String Effect;
        private List<String> Action=new LinkedList<>();
        private List<String> Resource=new LinkedList<>();

        public String getEffect() {
            return Effect;
        }

        public StatementBean setEffect(String Effect) {
            this.Effect = Effect;
            return this;
        }

        public List<String> getAction() {
            return Action;
        }

        public StatementBean setAction(List<String> Action) {
            this.Action = Action;
            return this;
        }

        public List<String> getResource() {
            return Resource;
        }

        public StatementBean setResource(List<String> Resource) {
            this.Resource = Resource;
            return this;
        }
    }

}
