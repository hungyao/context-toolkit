package context.arch.logging.hibernate;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Column;
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
public class CUDestination implements Serializable {

	private static final long serialVersionUID = -279074572212854334L;

    @Id 
    @GeneratedValue(strategy=GenerationType.IDENTITY)
	/** identifier field */
    private Integer cudestinationid;

    /** persistent field */
    private String destinationcomponentid;

    @Column(nullable = true)
    /** nullable persistent field */
    private Boolean success;

    @ManyToOne
	@JoinColumn(name = "componentupdateid")
    /** persistent field */
    private context.arch.logging.hibernate.ComponentUpdate ComponentUpdate;

	@OneToMany(fetch=FetchType.LAZY)
    /** persistent field */
    private Set<CUAttribute> CUAttributes;

    /** full constructor */
    public CUDestination(String destinationcomponentid, Boolean success, context.arch.logging.hibernate.ComponentUpdate ComponentUpdate, Set<CUAttribute> CUAttributes) {
        this.destinationcomponentid = destinationcomponentid;
        this.success = success;
        this.ComponentUpdate = ComponentUpdate;
        this.CUAttributes = CUAttributes;
    }

    /** default constructor */
    public CUDestination() {
    }

    /** minimal constructor */
    public CUDestination(String destinationcomponentid, context.arch.logging.hibernate.ComponentUpdate ComponentUpdate, Set<CUAttribute> CUAttributes) {
        this.destinationcomponentid = destinationcomponentid;
        this.ComponentUpdate = ComponentUpdate;
        this.CUAttributes = CUAttributes;
    }

    public Integer getCudestinationid() {
        return this.cudestinationid;
    }

    public void setCudestinationid(Integer cudestinationid) {
        this.cudestinationid = cudestinationid;
    }

    public String getDestinationcomponentid() {
        return this.destinationcomponentid;
    }

    public void setDestinationcomponentid(String destinationcomponentid) {
        this.destinationcomponentid = destinationcomponentid;
    }

    public Boolean getSuccess() {
        return this.success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public context.arch.logging.hibernate.ComponentUpdate getComponentUpdate() {
        return this.ComponentUpdate;
    }

    public void setComponentUpdate(context.arch.logging.hibernate.ComponentUpdate ComponentUpdate) {
        this.ComponentUpdate = ComponentUpdate;
    }

    public Set<CUAttribute> getCUAttributes() {
        return this.CUAttributes;
    }

    public void setCUAttributes(Set<CUAttribute> CUAttributes) {
        this.CUAttributes = CUAttributes;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("cudestinationid", getCudestinationid())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof CUDestination) ) return false;
        CUDestination castOther = (CUDestination) other;
        return new EqualsBuilder()
            .append(this.getCudestinationid(), castOther.getCudestinationid())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getCudestinationid())
            .toHashCode();
    }

}
