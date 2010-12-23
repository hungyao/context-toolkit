package context.arch.logging.hibernate;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
@Entity
public class ServiceExecution implements Serializable {

	private static final long serialVersionUID = 4684825482470233630L;

    @Id 
    @GeneratedValue(strategy=GenerationType.IDENTITY)
	/** identifier field */
    private Integer serviceexecutionid;

    /** persistent field */
    private String servicename;

    /** persistent field */
    private String functionname;

    /** persistent field */
    private Date executiontime;

    @ManyToOne
	@JoinColumn(name = "componentaddedid")
    /** persistent field */
    private context.arch.logging.hibernate.ComponentAdded ComponentAdded;

    @ManyToOne
	@JoinColumn(name = "enactorregistrationid")
    /** persistent field */
    private context.arch.logging.hibernate.EnactorRegistration EnactorRegistration;

	@OneToMany(fetch=FetchType.LAZY)
    /** persistent field */
    private Set<SEInputAttribute> SEInputAttributes;

    /** full constructor */
    public ServiceExecution(String servicename, String functionname, Date executiontime, context.arch.logging.hibernate.ComponentAdded ComponentAdded, context.arch.logging.hibernate.EnactorRegistration EnactorRegistration, Set<SEInputAttribute> SEInputAttributes) {
        this.servicename = servicename;
        this.functionname = functionname;
        this.executiontime = executiontime;
        this.ComponentAdded = ComponentAdded;
        this.EnactorRegistration = EnactorRegistration;
        this.SEInputAttributes = SEInputAttributes;
    }

    /** default constructor */
    public ServiceExecution() {
    }

    public Integer getServiceexecutionid() {
        return this.serviceexecutionid;
    }

    public void setServiceexecutionid(Integer serviceexecutionid) {
        this.serviceexecutionid = serviceexecutionid;
    }

    public String getServicename() {
        return this.servicename;
    }

    public void setServicename(String servicename) {
        this.servicename = servicename;
    }

    public String getFunctionname() {
        return this.functionname;
    }

    public void setFunctionname(String functionname) {
        this.functionname = functionname;
    }

    public Date getExecutiontime() {
        return this.executiontime;
    }

    public void setExecutiontime(Date executiontime) {
        this.executiontime = executiontime;
    }

    public context.arch.logging.hibernate.ComponentAdded getComponentAdded() {
        return this.ComponentAdded;
    }

    public void setComponentAdded(context.arch.logging.hibernate.ComponentAdded ComponentAdded) {
        this.ComponentAdded = ComponentAdded;
    }

    public context.arch.logging.hibernate.EnactorRegistration getEnactorRegistration() {
        return this.EnactorRegistration;
    }

    public void setEnactorRegistration(context.arch.logging.hibernate.EnactorRegistration EnactorRegistration) {
        this.EnactorRegistration = EnactorRegistration;
    }

    public Set<SEInputAttribute> getSEInputAttributes() {
        return this.SEInputAttributes;
    }

    public void setSEInputAttributes(Set<SEInputAttribute> SEInputAttributes) {
        this.SEInputAttributes = SEInputAttributes;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("serviceexecutionid", getServiceexecutionid())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof ServiceExecution) ) return false;
        ServiceExecution castOther = (ServiceExecution) other;
        return new EqualsBuilder()
            .append(this.getServiceexecutionid(), castOther.getServiceexecutionid())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getServiceexecutionid())
            .toHashCode();
    }

}
