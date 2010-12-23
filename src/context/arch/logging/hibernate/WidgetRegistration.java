package context.arch.logging.hibernate;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
@Entity
public class WidgetRegistration implements Serializable {

	private static final long serialVersionUID = -151224553468482290L;

    @Id 
    @GeneratedValue(strategy=GenerationType.IDENTITY)
	/** identifier field */
    private Integer widgetregistrationid;

    /** persistent field */
    private String widgetid;

    /** persistent field */
    private Date registrationtime;

	@OneToMany(fetch=FetchType.LAZY)
    /** persistent field */
    private Set<WRCallback> WRCallbacks;

	@OneToMany(fetch=FetchType.LAZY)
    /** persistent field */
    private Set<WRAttribute> WRAttributes;

	@OneToMany(fetch=FetchType.LAZY)
    /** persistent field */
    private Set<WRService> WRServices;
    
    /** Used to prevent use of a magic number */
    public static final int CHILDREN_COUNT = 3;

    /** full constructor */
    public WidgetRegistration(String widgetid, Date registrationtime, Set<WRCallback> WRCallbacks, Set<WRAttribute> WRAttributes, Set<WRService> WRServices) {
        this.widgetid = widgetid;
        this.registrationtime = registrationtime;
        this.WRCallbacks = WRCallbacks;
        this.WRAttributes = WRAttributes;
        this.WRServices = WRServices;
    }

    /** default constructor */
    public WidgetRegistration() {
    }

    public Integer getWidgetregistrationid() {
        return this.widgetregistrationid;
    }

    public void setWidgetregistrationid(Integer widgetregistrationid) {
        this.widgetregistrationid = widgetregistrationid;
    }

    public String getWidgetid() {
        return this.widgetid;
    }

    public void setWidgetid(String widgetid) {
        this.widgetid = widgetid;
    }

    public Date getRegistrationtime() {
        return this.registrationtime;
    }

    public void setRegistrationtime(Date registrationtime) {
        this.registrationtime = registrationtime;
    }

    public Set<WRCallback> getWRCallbacks() {
        return this.WRCallbacks;
    }

    public void setWRCallbacks(Set<WRCallback> WRCallbacks) {
        this.WRCallbacks = WRCallbacks;
    }

    public Set<WRAttribute> getWRAttributes() {
        return this.WRAttributes;
    }

    public void setWRAttributes(Set<WRAttribute> WRAttributes) {
        this.WRAttributes = WRAttributes;
    }

    public Set<WRService> getWRServices() {
        return this.WRServices;
    }

    public void setWRServices(Set<WRService> WRServices) {
        this.WRServices = WRServices;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("widgetregistrationid", getWidgetregistrationid())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof WidgetRegistration) ) return false;
        WidgetRegistration castOther = (WidgetRegistration) other;
        return new EqualsBuilder()
            .append(this.getWidgetregistrationid(), castOther.getWidgetregistrationid())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getWidgetregistrationid())
            .toHashCode();
    }

}
