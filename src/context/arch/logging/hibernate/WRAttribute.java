package context.arch.logging.hibernate;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
@Entity
public class WRAttribute implements Serializable {

	private static final long serialVersionUID = 7081927248190952438L;

    @Id 
    @GeneratedValue(strategy=GenerationType.IDENTITY)
	/** identifier field */
    private Integer wrattributeid;

    /** persistent field */
    private String attributename;

    /** persistent field */
    private String attributetype;

    @Column(nullable = true)
    /** nullable persistent field */
    private String attributevaluestring;

    @Column(nullable = true)
    /** nullable persistent field */
    private Float attributevaluenumeric;

    /** persistent field */
    private boolean constant;

    @ManyToOne
	@JoinColumn(name = "widgetregistrationid")
    /** persistent field */
    private context.arch.logging.hibernate.WidgetRegistration WidgetRegistration;

    /** full constructor */
    public WRAttribute(String attributename, String attributetype, String attributevaluestring, Float attributevaluenumeric, boolean constant, context.arch.logging.hibernate.WidgetRegistration WidgetRegistration) {
        this.attributename = attributename;
        this.attributetype = attributetype;
        this.attributevaluestring = attributevaluestring;
        this.attributevaluenumeric = attributevaluenumeric;
        this.constant = constant;
        this.WidgetRegistration = WidgetRegistration;
    }

    /** default constructor */
    public WRAttribute() {
    }

    /** minimal constructor */
    public WRAttribute(String attributename, String attributetype, boolean constant, context.arch.logging.hibernate.WidgetRegistration WidgetRegistration) {
        this.attributename = attributename;
        this.attributetype = attributetype;
        this.constant = constant;
        this.WidgetRegistration = WidgetRegistration;
    }

    public Integer getWrattributeid() {
        return this.wrattributeid;
    }

    public void setWrattributeid(Integer wrattributeid) {
        this.wrattributeid = wrattributeid;
    }

    public String getAttributename() {
        return this.attributename;
    }

    public void setAttributename(String attributename) {
        this.attributename = attributename;
    }

    public String getAttributetype() {
        return this.attributetype;
    }

    public void setAttributetype(Class<?> attributetype) {
        this.attributetype = attributetype.getName();
    }

    public String getAttributevaluestring() {
        return this.attributevaluestring;
    }

    public void setAttributevaluestring(String attributevaluestring) {
        this.attributevaluestring = attributevaluestring;
    }

    public Float getAttributevaluenumeric() {
        return this.attributevaluenumeric;
    }

    public void setAttributevaluenumeric(Float attributevaluenumeric) {
        this.attributevaluenumeric = attributevaluenumeric;
    }

    public boolean isConstant() {
        return this.constant;
    }

    public void setConstant(boolean constant) {
        this.constant = constant;
    }

    public context.arch.logging.hibernate.WidgetRegistration getWidgetRegistration() {
        return this.WidgetRegistration;
    }

    public void setWidgetRegistration(context.arch.logging.hibernate.WidgetRegistration WidgetRegistration) {
        this.WidgetRegistration = WidgetRegistration;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("wrattributeid", getWrattributeid())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof WRAttribute) ) return false;
        WRAttribute castOther = (WRAttribute) other;
        return new EqualsBuilder()
            .append(this.getWrattributeid(), castOther.getWrattributeid())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getWrattributeid())
            .toHashCode();
    }

}
