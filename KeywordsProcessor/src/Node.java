
public class Node {

	private String id;
	private String municipio;
	private String name;
	private String date;
	private String periodicity;
	private String group;
	private String modality;
	private String size;
	private String fundings;
	private String entity;
	private String entityType;
	private String description;
		
	public Node(String id, String municipio, String name, String date, String periodicity, String group,
			String modality, String size, String fundings, String entity, String entityType, String description) {

		this.id = id;
		this.municipio = municipio;
		this.name = name;
		this.date = date;
		this.periodicity = periodicity;
		this.group = group;
		this.modality = modality;
		this.size = size;
		this.fundings = fundings;
		this.entity = entity;
		this.entityType = entityType;
		this.description = description;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getMunicipio() {
		return municipio;
	}
	public void setMunicipio(String municipio) {
		this.municipio = municipio;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getPeriodicity() {
		return periodicity;
	}
	public void setPeriodicity(String periodicity) {
		this.periodicity = periodicity;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public String getModality() {
		return modality;
	}
	public void setModality(String modality) {
		this.modality = modality;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	public String getFundings() {
		return fundings;
	}
	public void setFundings(String fundings) {
		this.fundings = fundings;
	}
	public String getEntity() {
		return entity;
	}
	public void setEntity(String entity) {
		this.entity = entity;
	}
	public String getEntityType() {
		return entityType;
	}
	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	

}
