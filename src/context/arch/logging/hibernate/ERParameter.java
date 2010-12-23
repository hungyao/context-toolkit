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
public class ERParameter implements Serializable {

	private static final long serialVersionUID = -7927170427262347114L;

	@Id 
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	/** identifier field */
	private Integer erparameterid;

	/** persistent field */
	private String parametername;

    @ManyToOne
	@JoinColumn(name = "enactorregistrationid")
	/** persistent field */
	private context.arch.logging.hibernate.EnactorRegistration EnactorRegistration;

	@OneToMany(fetch=FetchType.LAZY)
	/** persistent field */
	private Set<ParameterValueChanged> ParameterValuesChanged;

	/** full constructor */
	public ERParameter(String parametername, context.arch.logging.hibernate.EnactorRegistration EnactorRegistration, Set<ParameterValueChanged> ParameterValuesChanged) {
		this.parametername = parametername;
		this.EnactorRegistration = EnactorRegistration;
		this.ParameterValuesChanged = ParameterValuesChanged;
	}

	/** default constructor */
	public ERParameter() {
	}

	public Integer getErparameterid() {
		return this.erparameterid;
	}

	public void setErparameterid(Integer erparameterid) {
		this.erparameterid = erparameterid;
	}

	public String getParametername() {
		return this.parametername;
	}

	public void setParametername(String parametername) {
		this.parametername = parametername;
	}

	public context.arch.logging.hibernate.EnactorRegistration getEnactorRegistration() {
		return this.EnactorRegistration;
	}

	public void setEnactorRegistration(context.arch.logging.hibernate.EnactorRegistration EnactorRegistration) {
		this.EnactorRegistration = EnactorRegistration;
	}

	public Set<ParameterValueChanged> getParameterValuesChanged() {
		return this.ParameterValuesChanged;
	}

	public void setParameterValuesChanged(Set<ParameterValueChanged> ParameterValuesChanged) {
		this.ParameterValuesChanged = ParameterValuesChanged;
	}

	public String toString() {
		return new ToStringBuilder(this)
		.append("erparameterid", getErparameterid())
		.toString();
	}

	public boolean equals(Object other) {
		if ( !(other instanceof ERParameter) ) return false;
		ERParameter castOther = (ERParameter) other;
		return new EqualsBuilder()
		.append(this.getErparameterid(), castOther.getErparameterid())
		.isEquals();
	}

	public int hashCode() {
		return new HashCodeBuilder()
		.append(getErparameterid())
		.toHashCode();
	}

}
