package org.mcupdater.auth;


import java.util.Date;

public class XBLToken {
    private Date IssueInstant;
    private Date NotAfter;
    private String Token;
    private DisplayClaims DisplayClaims;

    public Date getIssueInstant() {
        return IssueInstant;
    }

    public void setIssueInstant(Date IssueInstant) {
        this.IssueInstant = IssueInstant;
    }

    public Date getNotAfter() {
        return NotAfter;
    }

    public void setNotAfter(Date NotAfter) {
        this.NotAfter = NotAfter;
    }

    public String getToken() {
        return Token;
    }

    public void setToken(String Token) {
        this.Token = Token;
    }

    public DisplayClaims getDisplayClaims() {
        return DisplayClaims;
    }

    public void setDisplayClaims(DisplayClaims DisplayClaims) {
        this.DisplayClaims = DisplayClaims;
    }

    public class DisplayClaims {
        private Xui[] xui;

        public Xui[] getXui() {
            return xui;
        }

        public void setXui(Xui[] xui) {
            this.xui = xui;
        }
    }

    public class Xui {
        private String uhs;

        public String getUhs() {
            return uhs;
        }

        public void setUhs(String uhs) {
            this.uhs = uhs;
        }
    }
}
