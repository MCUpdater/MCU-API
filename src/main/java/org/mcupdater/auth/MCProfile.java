package org.mcupdater.auth;


public class MCProfile {
    private String id;
    private String name;
    private Skin[] skins;
    private Cape[] capes;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Skin[] getSkins() {
        return skins;
    }

    public void setSkins(Skin[] skins) {
        this.skins = skins;
    }

    public Cape[] getCapes() {
        return capes;
    }

    public void setCapes(Cape[] capes) {
        this.capes = capes;
    }

    public class Cape {
        private String id;
        private String state;
        private String url;
        private String alias;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getAlias() {
            return alias;
        }

        public void setAlias(String alias) {
            this.alias = alias;
        }
    }

    public class Skin {
        private String id;
        private String state;
        private String url;
        private String variant;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getVariant() {
            return variant;
        }

        public void setVariant(String variant) {
            this.variant = variant;
        }
    }
}
