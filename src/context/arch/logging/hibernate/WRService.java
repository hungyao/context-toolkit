package context.arch.logging.hibernate;

import java.io.Serializable;
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
public class WRService implements Serializable {

	private static final long serialVersionUID = -9025083461715098243L;

    @Id 
    @GeneratedValue(strategy=GenerationType.IDENTITY)
	/** identifier field */
    private Integer wrserviceid;

    /** persistent field */
    private String servicename;

    @ManyToOne
	@JoinColumn(name = "widgetregistrationid")
    /** persistent field */
    private context.arch.logging.hibernate.WidgetRegistration WidgetRegistration;

	@OneToMany(fetch=FetchType.LAZY)
    /** persistent field */
    private Set<WRServiceFunction> WRServiceFunctions;

    /** full constructor */
    public WRService(String servicename, context.arch.logging.hibernate.WidgetRegistration WidgetRegistration, Set<WRServiceFunction> WRServiceFunctions) {
        this.servicename = servicename;
        this.WidgetRegistration = WidgetRegistration;
        this.WRServiceFunctions = WRServiceFunctions;
    }

    /** default constructor */
    public WRService() {
    }

    public Integer getWrserviceid() {
        return this.wrserviceid;
    }

    public void setWrserviceid(Integer wrserviceid) {
        this.wrserviceid = wrserviceid;
    }

    public String getServicename() {
        return this.servicename;
    }

    public void setServicename(String servicename) {
        this.servicename = servicename;
    }

    public context.arch.logging.hibernate.WidgetRegistration getWidgetRegistration() {
        return this.WidgetRegistration;
    }

    public void setWidgetRegistration(context.arch.logging.hibernate.WidgetRegistration WidgetRegistration) {
        this.WidgetRegistration = WidgetRegistration;
    }

    public Set<WRServiceFunction> getWRServiceFunctions() {
        return this.WRServiceFunctions;
    }

    public void setWRServiceFunctions(Set<WRServiceFunction> WRServiceFunctions) {
        this.WRServiceFunctions = WRServiceFunctions;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("wrserviceid", getWrserviceid())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof WRService) ) return false;
        WRService castOther = (WRService) other;
        return new EqualsBuilder()
            .append(this.getWrserviceid(), castOther.getWrserviceid())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getWrserviceid())
            .toHashCode();
    }

}
