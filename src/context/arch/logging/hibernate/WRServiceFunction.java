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
public class WRServiceFunction implements Serializable {

	private static final long serialVersionUID = -6915146097071133276L;

    @Id 
    @GeneratedValue(strategy=GenerationType.IDENTITY)
	/** identifier field */
    private Integer wrservicefunctionid;

    @Column(nullable = true)
    /** nullable persistent field */
    private String functionname;

    @Column(nullable = true)
    /** nullable persistent field */
    private String functiondescription;

    @ManyToOne
	@JoinColumn(name = "wrserviceid")
    /** persistent field */
    private context.arch.logging.hibernate.WRService WRService;

    /** full constructor */
    public WRServiceFunction(String functionname, String functiondescription, context.arch.logging.hibernate.WRService WRService) {
        this.functionname = functionname;
        this.functiondescription = functiondescription;
        this.WRService = WRService;
    }

    /** default constructor */
    public WRServiceFunction() {
    }

    /** minimal constructor */
    public WRServiceFunction(context.arch.logging.hibernate.WRService WRService) {
        this.WRService = WRService;
    }

    public Integer getWrservicefunctionid() {
        return this.wrservicefunctionid;
    }

    public void setWrservicefunctionid(Integer wrservicefunctionid) {
        this.wrservicefunctionid = wrservicefunctionid;
    }

    public String getFunctionname() {
        return this.functionname;
    }

    public void setFunctionname(String functionname) {
        this.functionname = functionname;
    }

    public String getFunctiondescription() {
        return this.functiondescription;
    }

    public void setFunctiondescription(String functiondescription) {
        this.functiondescription = functiondescription;
    }

    public context.arch.logging.hibernate.WRService getWRService() {
        return this.WRService;
    }

    public void setWRService(context.arch.logging.hibernate.WRService WRService) {
        this.WRService = WRService;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("wrservicefunctionid", getWrservicefunctionid())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof WRServiceFunction) ) return false;
        WRServiceFunction castOther = (WRServiceFunction) other;
        return new EqualsBuilder()
            .append(this.getWrservicefunctionid(), castOther.getWrservicefunctionid())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getWrservicefunctionid())
            .toHashCode();
    }

}
