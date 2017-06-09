package org.endeavourhealth.informationmodel.api.database.models;

import javax.persistence.*;

/**
 * Created by studu on 09/06/2017.
 */
@Entity
@Table(name = "concept_relationship", schema = "information_model", catalog = "")
public class ConceptRelationshipEntity {
    private int id;
    private long sourceConcept;
    private long targetConcept;
    private String targetLabel;
    private Integer relationshipOrder;
    private Long relationshipType;
    private Integer contextId;
    private Long count;

    @Id
    @Column(name = "id", nullable = false)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "source_concept", nullable = false)
    public long getSourceConcept() {
        return sourceConcept;
    }

    public void setSourceConcept(long sourceConcept) {
        this.sourceConcept = sourceConcept;
    }

    @Basic
    @Column(name = "target_concept", nullable = false)
    public long getTargetConcept() {
        return targetConcept;
    }

    public void setTargetConcept(long targetConcept) {
        this.targetConcept = targetConcept;
    }

    @Basic
    @Column(name = "target_label", nullable = true, length = 45)
    public String getTargetLabel() {
        return targetLabel;
    }

    public void setTargetLabel(String targetLabel) {
        this.targetLabel = targetLabel;
    }

    @Basic
    @Column(name = "relationship_order", nullable = true)
    public Integer getRelationshipOrder() {
        return relationshipOrder;
    }

    public void setRelationshipOrder(Integer relationshipOrder) {
        this.relationshipOrder = relationshipOrder;
    }

    @Basic
    @Column(name = "relationship_type", nullable = true)
    public Long getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(Long relationshipType) {
        this.relationshipType = relationshipType;
    }

    @Basic
    @Column(name = "context_id", nullable = true)
    public Integer getContextId() {
        return contextId;
    }

    public void setContextId(Integer contextId) {
        this.contextId = contextId;
    }

    @Basic
    @Column(name = "count", nullable = true)
    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConceptRelationshipEntity that = (ConceptRelationshipEntity) o;

        if (id != that.id) return false;
        if (sourceConcept != that.sourceConcept) return false;
        if (targetConcept != that.targetConcept) return false;
        if (targetLabel != null ? !targetLabel.equals(that.targetLabel) : that.targetLabel != null) return false;
        if (relationshipOrder != null ? !relationshipOrder.equals(that.relationshipOrder) : that.relationshipOrder != null)
            return false;
        if (relationshipType != null ? !relationshipType.equals(that.relationshipType) : that.relationshipType != null)
            return false;
        if (contextId != null ? !contextId.equals(that.contextId) : that.contextId != null) return false;
        if (count != null ? !count.equals(that.count) : that.count != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (int) (sourceConcept ^ (sourceConcept >>> 32));
        result = 31 * result + (int) (targetConcept ^ (targetConcept >>> 32));
        result = 31 * result + (targetLabel != null ? targetLabel.hashCode() : 0);
        result = 31 * result + (relationshipOrder != null ? relationshipOrder.hashCode() : 0);
        result = 31 * result + (relationshipType != null ? relationshipType.hashCode() : 0);
        result = 31 * result + (contextId != null ? contextId.hashCode() : 0);
        result = 31 * result + (count != null ? count.hashCode() : 0);
        return result;
    }
}
