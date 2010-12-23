package context.arch.logging.hibernate;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
@Entity
public class ComponentSubscription implements Serializable {

	private static final long serialVersionUID = -1287048613015673239L;

    @Id 
    @GeneratedValue(strategy=GenerationType.IDENTITY)
	/** identifier field */
    private Integer componentsubscriptionid;

    /** persistent field */
    private String componentid;

    /** persistent field */
    private String subscriberid;

    @Column(nullable = true)
    /** nullable persistent field */
    private String condition;

    /** full constructor */
    public ComponentSubscription(String componentid, String subscriberid, String condition) {
        this.componentid = componentid;
        this.subscriberid = subscriberid;
        this.condition = condition;
    }

    /** default constructor */
    public ComponentSubscription() {
    }

    /** minimal constructor */
    public ComponentSubscription(String componentid, String subscriberid) {
        this.componentid = componentid;
        this.subscriberid = subscriberid;
    }

    public Integer getComponentsubscriptionid() {
        return this.componentsubscriptionid;
    }

    public void setComponentsubscriptionid(Integer componentsubscriptionid) {
        this.componentsubscriptionid = componentsubscriptionid;
    }

    public String getComponentid() {
        return this.componentid;
    }

    public void setComponentid(String componentid) {
        this.componentid = componentid;
    }

    public String getSubscriberid() {
        return this.subscriberid;
    }

    public void setSubscriberid(String subscriberid) {
        this.subscriberid = subscriberid;
    }

    public String getCondition() {
        return this.condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("componentsubscriptionid", getComponentsubscriptionid())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof ComponentSubscription) ) return false;
        ComponentSubscription castOther = (ComponentSubscription) other;
        return new EqualsBuilder()
            .append(this.getComponentsubscriptionid(), castOther.getComponentsubscriptionid())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getComponentsubscriptionid())
            .toHashCode();
    }

}
