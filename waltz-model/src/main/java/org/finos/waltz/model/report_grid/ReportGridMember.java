package org.finos.waltz.model.report_grid;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableReportGridMember.class)
@JsonDeserialize(as = ImmutableReportGridMember.class)
public abstract class ReportGridMember {

    public abstract Long gridId();
    public abstract String userId();

    @Value.Default
    public ReportGridMemberRole role() {
        return ReportGridMemberRole.VIEWER;
    }

}
