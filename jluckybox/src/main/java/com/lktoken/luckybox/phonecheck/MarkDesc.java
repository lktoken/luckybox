package com.lktoken.luckybox.phonecheck;


public class MarkDesc {

    /**
     * 标记类型
     *
     * <pre>
     * 违法电话
     * 广告推销
     * 推销电话
     * 房产中介
     * 诈骗电话
     * 疑似欺诈！谨防上当受骗！
     * 保险理财
     * 骚扰电话
     * </pre>
     */

    public enum MarkType {
        ILLEGAL_CALL("违法电话"), SALES_CALL("推销电话"), REAL_ESTATE_AGENT("房产中介"), FRAUD_PHONE("诈骗电话"),
        INSURANCE_FINACIAL("保险理财"), CRANK_CALL("骚扰电话"), OTHER("其他标记");

        private String displayName;

        MarkType(String displayName){
            this.displayName = displayName;
        }

        public static MarkType of(String desc) {
            if (desc == null) {
                return null;
            }
            if (desc.contains("违法")) {
                return ILLEGAL_CALL;
            } else if (desc.contains("推销")) {
                return SALES_CALL;
            } else if (desc.contains("房产")) {
                return REAL_ESTATE_AGENT;
            } else if (desc.contains("诈骗") || desc.contains("欺诈")) {
                return FRAUD_PHONE;
            } else if (desc.contains("保险") || desc.contains("理财")) {
                return INSURANCE_FINACIAL;
            } else if (desc.contains("骚扰")) {
                return CRANK_CALL;
            }

            return OTHER;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 标记来源
     */
    private String source;

    /**
     * 提供标记查询的网站
     */
    private String provider;

    /**
     * 标记文本
     */
    private String mark;

    /**
     * 标记文本转为枚举类型后的标记类型 当有新增未识别类型时，会被标记为OTHER，
     *
     * @see MarkType#of(java.lang.String)
     */
    private MarkType markType;

    /**
     * 被标记为的选项
     */
    private String markAs;

    /**
     * 标记次数
     */
    private int      count;

    /**
     * 标记描述的原始文本
     */
    private String originDesc;

    public MarkDesc(String provider){
        this.provider = provider;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        setMarkType(mark);
        this.mark = mark;
    }

    public String getMarkAs() {
        return markAs;
    }

    public void setMarkAs(String markAs) {
        this.markAs = markAs;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getOriginDesc() {
        return originDesc;
    }

    public void setOriginDesc(String originDesc) {
        this.originDesc = originDesc;
    }

    public MarkType getMarkType() {
        return markType;
    }

    public void setMarkType(String markType) {
        this.markType = MarkType.of(markType);
    }

    public void setMarkType(MarkType markType) {
        this.markType = markType;
    }
}
