package context.arch.logging.hibernate;

import java.io.Serializable;

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
public class WRCallback implements Serializable {

	private static final long serialVersionUID = 1913698663268217496L;

    @Id 
    @GeneratedValue(strategy=GenerationType.IDENTITY)
	/** identifier field */
    private Integer wrcallbackid;

    /** persistent field */
    private String callbackname;

    @ManyToOne
	@JoinColumn(name = "widgetregistrationid")
    /** persistent field */
    private context.arch.logging.hibernate.WidgetRegistration WidgetRegistration;

    /** full constructor */
    public WRCallback(String callbackname, context.arch.logging.hibernate.WidgetRegistration WidgetRegistration) {
        this.callbackname = callbackname;
        this.WidgetRegistration = WidgetRegistration;
    }

    /** default constructor */
    public WRCallback() {
    }

    public Integer getWrcallbackid() {
        return this.wrcallbackid;
    }

    public void setWrcallbackid(Integer wrcallbackid) {
        this.wrcallbackid = wrcallbackid;
    }

    public String getCallbackname() {
        return this.callbackname;
    }

    public void setCallbackname(String callbackname) {
        this.callbackname = callbackname;
    }

    public context.arch.logging.hibernate.WidgetRegistration getWidgetRegistration() {
        return this.WidgetRegistration;
    }

    public void setWidgetRegistration(context.arch.logging.hibernate.WidgetRegistration WidgetRegistration) {
        this.WidgetRegistration = WidgetRegistration;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("wrcallbackid", getWrcallbackid())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof WRCallback) ) return false;
        WRCallback castOther = (WRCallback) other;
        return new EqualsBuilder()
            .append(this.getWrcallbackid(), castOther.getWrcallbackid())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getWrcallbackid())
            .toHashCode();
    }

}
