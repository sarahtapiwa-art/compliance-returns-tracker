package zw.co.nbs.returnstrackingsystem.convertor;

import org.mapstruct.Mapper;
import zw.co.nbs.returnstrackingsystem.dtos.response.ScheduleRuleResponse;
import zw.co.nbs.returnstrackingsystem.domain.ScheduleRule;

/**
 * createdBy       lorraine.mhizha
 * createdDate     25/8/2025
 * createdTime     12:03
 * projectName     compliance-returns-tracker
 **/
@Mapper(componentModel = "spring")
public interface ScheduleRuleObjectMapper {

    ScheduleRuleResponse toScheduleRuleResponse(ScheduleRule scheduleRule);
}
