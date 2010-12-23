package context.arch.logging.hibernate;

import java.io.Serializable;
import java.util.Date;

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
public class ComponentEvaluated implements Serializable {

	private static final long serialVersionUID = 4608086694247335902L;

    @Id 
    @GeneratedValue(strategy=GenerationType.IDENTITY)
	/** identifier field */
    private Integer componentevaluatedid;

    /** persistent field */
    private String componentdescriptionid;

    /** persistent field */
    private Date componentevaluatedtime;

    @ManyToOne
	@JoinColumn(name = "erreferenceid")
    /** persistent field */
    private context.arch.logging.hibernate.ERReference ERReference;

    @ManyToOne
	@JoinColumn(name = "enactorregistrationid")
    /** persistent field */
    private context.arch.logging.hibernate.EnactorRegistration EnactorRegistration;

    /** full constructor */
    public ComponentEvaluated(String componentdescriptionid, Date componentevaluatedtime, context.arch.logging.hibernate.ERReference ERReference, context.arch.logging.hibernate.EnactorRegistration EnactorRegistration) {
        this.componentdescriptionid = componentdescriptionid;
        this.componentevaluatedtime = componentevaluatedtime;
        this.ERReference = ERReference;
        this.EnactorRegistration = EnactorRegistration;
    }

    /** default constructor */
    public ComponentEvaluated() {
    }

    public Integer getComponentevaluatedid() {
        return this.componentevaluatedid;
    }

    public void setComponentevaluatedid(Integer componentevaluatedid) {
        this.componentevaluatedid = componentevaluatedid;
    }

    public String getComponentdescriptionid() {
        return this.componentdescriptionid;
    }

    public void setComponentdescriptionid(String componentdescriptionid) {
        this.componentdescriptionid = componentdescriptionid;
    }

    public Date getComponentevaluatedtime() {
        return this.componentevaluatedtime;
    }

    public void setComponentevaluatedtime(Date componentevaluatedtime) {
        this.componentevaluatedtime = componentevaluatedtime;
    }

    public context.arch.logging.hibernate.ERReference getERReference() {
        return this.ERReference;
    }

    public void setERReference(context.arch.logging.hibernate.ERReference ERReference) {
        this.ERReference = ERReference;
    }

    public context.arch.logging.hibernate.EnactorRegistration getEnactorRegistration() {
        return this.EnactorRegistration;
    }

    public void setEnactorRegistration(context.arch.logging.hibernate.EnactorRegistration EnactorRegistration) {
        this.EnactorRegistration = EnactorRegistration;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("componentevaluatedid", getComponentevaluatedid())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof ComponentEvaluated) ) return false;
        ComponentEvaluated castOther = (ComponentEvaluated) other;
        return new EqualsBuilder()
            .append(this.getComponentevaluatedid(), castOther.getComponentevaluatedid())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getComponentevaluatedid())
            .toHashCode();
    }

}
