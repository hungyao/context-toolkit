package context.arch.util;

/**
 * This class implements a context user object, encapsulating a user's 
 * description (usually a name), their full name, email address,
 * organization, and iButton id.
 *
 * @see context.arch.util.ContextUsers
 */
public class ContextUser {

  private String description;
  private String name;
  private String email;
  private String organization;
  private String ibuttonid;

  /**
   * Basic constructor
   *
   * @param description Description of the user (usually a name)
   * @param name Full name of the user
   * @param email Email address of the user
   * @param organization Organization the user belongs to
   * @param ibuttonid iButton id of the user
   */
  public ContextUser(String description, String name, String email, 
                     String organization, String ibuttonid) {
    this.description = description;
    this.name = name;
    this.email = email;
    this.organization = organization;
    this.ibuttonid = ibuttonid;
  }

  /**
   * Returns the user's description
   *
   * @return the user's description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the user's description
   *
   * @param description the user's description
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Returns the user's name
   *
   * @return the user's name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the user's name
   *
   * @param name the user's name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the user's email address
   *
   * @return the user's email address
   */
  public String getEmail() {
    return email;
  }

  /**
   * Sets the user's email address
   *
   * @param email the user's email address
   */
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * Returns the user's organization
   *
   * @return the user's organization
   */
  public String getOrganization() {
    return organization;
  }

  /**
   * Sets the user's organization
   *
   * @param organization the user's organization
   */
  public void setOrganization(String organization) {
    this.organization = organization;
  }

  /**
   * Returns the user's iButton id
   *
   * @return the user's ibutton id
   */
  public String getIButtonId() {
    return ibuttonid;
  }

  /**
   * Sets the user's iButton id
   *
   * @param ibuttonid the user's iButton id
   */
  public void setIButtonId(String ibuttonid) {
    this.ibuttonid = ibuttonid;
  }

}
