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
public class ERReference implements Serializable {

	private static final long serialVersionUID = 4640604609765382173L;

    @Id 
    @GeneratedValue(strategy=GenerationType.IDENTITY)
	/** identifier field */
    private Integer erreferenceid;

    @Column(nullable = true)
    /** nullable persistent field */
    private String descriptionquery;

    @ManyToOne
	@JoinColumn(name = "enactorregistrationid")
    /** persistent field */
    private context.arch.logging.hibernate.EnactorRegistration EnactorRegistration;

	@OneToMany(fetch=FetchType.LAZY)
    /** persistent field */
    private Set<ComponentEvaluated> ComponentsEvaluated;

	@OneToMany(fetch=FetchType.LAZY)
    /** persistent field */
    private Set<ComponentAdded> ComponentsAdded;

    /** full constructor */
    public ERReference(String descriptionquery, context.arch.logging.hibernate.EnactorRegistration EnactorRegistration, Set<ComponentEvaluated> ComponentsEvaluated, Set<ComponentAdded> ComponentsAdded) {
        this.descriptionquery = descriptionquery;
        this.EnactorRegistration = EnactorRegistration;
        this.ComponentsEvaluated = ComponentsEvaluated;
        this.ComponentsAdded = ComponentsAdded;
    }

    /** default constructor */
    public ERReference() {
    }

    /** minimal constructor */
    public ERReference(context.arch.logging.hibernate.EnactorRegistration EnactorRegistration, Set<ComponentEvaluated> ComponentsEvaluated, Set<ComponentAdded> ComponentsAdded) {
        this.EnactorRegistration = EnactorRegistration;
        this.ComponentsEvaluated = ComponentsEvaluated;
        this.ComponentsAdded = ComponentsAdded;
    }

    public Integer getErreferenceid() {
        return this.erreferenceid;
    }

    public void setErreferenceid(Integer erreferenceid) {
        this.erreferenceid = erreferenceid;
    }

    public String getDescriptionquery() {
        return this.descriptionquery;
    }

    public void setDescriptionquery(String descriptionquery) {
        this.descriptionquery = descriptionquery;
    }

    public context.arch.logging.hibernate.EnactorRegistration getEnactorRegistration() {
        return this.EnactorRegistration;
    }

    public void setEnactorRegistration(context.arch.logging.hibernate.EnactorRegistration EnactorRegistration) {
        this.EnactorRegistration = EnactorRegistration;
    }

    public Set<ComponentEvaluated> getComponentsEvaluated() {
        return this.ComponentsEvaluated;
    }

    public void setComponentsEvaluated(Set<ComponentEvaluated> ComponentsEvaluated) {
        this.ComponentsEvaluated = ComponentsEvaluated;
    }

    public Set<ComponentAdded> getComponentsAdded() {
        return this.ComponentsAdded;
    }

    public void setComponentsAdded(Set<ComponentAdded> ComponentsAdded) {
        this.ComponentsAdded = ComponentsAdded;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("erreferenceid", getErreferenceid())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof ERReference) ) return false;
        ERReference castOther = (ERReference) other;
        return new EqualsBuilder()
            .append(this.getErreferenceid(), castOther.getErreferenceid())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getErreferenceid())
            .toHashCode();
    }

}
