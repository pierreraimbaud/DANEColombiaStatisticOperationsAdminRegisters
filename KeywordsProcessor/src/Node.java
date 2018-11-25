
public class Node {

	private String id;
	private String type;
	private String name;
	private String statisticalOperations;
	private String objective;
	private String unityObservation;
	private String entitiesUsing;
	private String variables;
	private String daneDependency;
	private String thematic;
	private String thematics2;
	private String thematicShared;
    private String entityProducing;
    private String periodicity;
    private String geographicZone;
    private String geographicDivision;
    private String indicators;
    private String methodologyOOEE;

    public Node(String id, String type, String name, String statisticalOperations, String objective, String unityObservation,
                String entitiesUsing, String variables, String daneDependency,String entityProducing,
				String indicators,String thematicShared, String periodicity, String geographicZone, String geographicDivision,
				String methodologyOOEE, String thematic, String thematics2) {

		this.id = id;
		this.type = type;
		this.name = name;
		this.statisticalOperations = statisticalOperations;
		this.objective = objective;
		this.unityObservation = unityObservation;
		this.entitiesUsing = entitiesUsing;
		this.variables = variables;
		this.daneDependency = daneDependency;
		this.thematicShared = thematicShared;
		this.entityProducing = entityProducing;
		this.periodicity = periodicity;
		this.geographicZone = geographicZone;
		this.geographicDivision = geographicDivision;
		this.indicators = indicators;
		this.methodologyOOEE = methodologyOOEE;
        this.thematic = thematic;
        this.thematics2 = thematics2;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getStatisticalOperations() {
		return statisticalOperations;
	}
	public void setStatisticalOperations(String statisticalOperations) {
		this.statisticalOperations = statisticalOperations;
	}
	public String getObjective() {
		return objective;
	}
	public void setObjective(String objective) {
		this.objective = objective;
	}
	public String getUnityObservation() {
		return unityObservation;
	}
	public void setUnityObservation(String group) {
		this.unityObservation = group;
	}
	public String getEntitiesUsing() {
		return entitiesUsing;
	}
	public void setEntitiesUsing(String entitiesUsing) {
		this.entitiesUsing = entitiesUsing;
	}
	public String getVariables() {
		return variables;
	}
	public void setVariables(String variables) {
		this.variables = variables;
	}
	public String getDaneDependency() {
		return daneDependency;
	}
	public void setDaneDependency(String daneDependency) {
		this.daneDependency = daneDependency;
	}
	public String getThematic() {
		return thematic;
	}
	public void setThematic(String thematic) {
		this.thematic = thematic;
	}
	public String getThematics2() {
		return thematics2;
	}
	public void setThematics2(String thematics2) {
		this.thematics2 = thematics2;
	}
	public String getThematicShared() {
		return thematicShared;
	}
	public void setThematicShared(String thematicShared) {
		this.thematicShared = thematicShared;
	}
    public String getEntityProducing() {
        return entityProducing;
    }

    public void setEntityProducing(String entityProducing) {
        this.entityProducing = entityProducing;
    }

    public String getPeriodicity() {
        return periodicity;
    }

    public void setPeriodicity(String periodicity) {
        this.periodicity = periodicity;
    }

    public String getGeographicZone() {
        return geographicZone;
    }

    public void setGeographicZone(String geographicZone) {
        this.geographicZone = geographicZone;
    }

    public String getGeographicDivision() {
        return geographicDivision;
    }

    public void setGeographicDivision(String geographicDivision) {
        this.geographicDivision = geographicDivision;
    }

    public String getIndicators() {
        return indicators;
    }

    public void setIndicators(String indicators) {
        this.indicators = indicators;
    }

    public String getMethodologyOOEE() {
        return methodologyOOEE;
    }

    public void setMethodologyOOEE(String methodologyOOEE) {
        this.methodologyOOEE = methodologyOOEE;
    }



}
