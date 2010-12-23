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
public class ComponentUpdate implements Serializable {

	private static final long serialVersionUID = -7100125623371450357L;

    @Id 
    @GeneratedValue(strategy=GenerationType.IDENTITY)
	/** identifier field */
    private Integer componentupdateid;

    /** persistent field */
    private String componentid;

    /** persistent field */
    private Date updatetime;

    /** persistent field */
    private String updatename;

	@OneToMany(fetch=FetchType.LAZY)
    /** persistent field */
    private Set<CUDestination> CUDestinations;

    /** full constructor */
    public ComponentUpdate(String componentid, Date updatetime, String updatename, Set<CUDestination> CUDestinations) {
        this.componentid = componentid;
        this.updatetime = updatetime;
        this.updatename = updatename;
        this.CUDestinations = CUDestinations;
    }

    /** default constructor */
    public ComponentUpdate() {
    }

    public Integer getComponentupdateid() {
        return this.componentupdateid;
    }

    public void setComponentupdateid(Integer componentupdateid) {
        this.componentupdateid = componentupdateid;
    }

    public String getComponentid() {
        return this.componentid;
    }

    public void setComponentid(String componentid) {
        this.componentid = componentid;
    }

    public Date getUpdatetime() {
        return this.updatetime;
    }

    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }

    public String getUpdatename() {
        return this.updatename;
    }

    public void setUpdatename(String updatename) {
        this.updatename = updatename;
    }

    public Set<CUDestination> getCUDestinations() {
        return this.CUDestinations;
    }

    public void setCUDestinations(Set<CUDestination> CUDestinations) {
        this.CUDestinations = CUDestinations;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("componentupdateid", getComponentupdateid())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof ComponentUpdate) ) return false;
        ComponentUpdate castOther = (ComponentUpdate) other;
        return new EqualsBuilder()
            .append(this.getComponentupdateid(), castOther.getComponentupdateid())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getComponentupdateid())
            .toHashCode();
    }

}
