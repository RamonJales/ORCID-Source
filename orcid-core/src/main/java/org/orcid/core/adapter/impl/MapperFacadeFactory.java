/**
 * =============================================================================
 *
 * ORCID (R) Open Source
 * http://orcid.org
 *
 * Copyright (c) 2012-2014 ORCID, Inc.
 * Licensed under an MIT-Style License (MIT)
 * http://orcid.org/open-source-license
 *
 * This copyright and license information (including a link to the full license)
 * shall be included in its entirety in all copies or substantial portion of
 * the software.
 *
 * =============================================================================
 */
package org.orcid.core.adapter.impl;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.converter.ConverterFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.ClassMapBuilder;

import org.orcid.jaxb.model.common.Source;
import org.orcid.jaxb.model.notification.addactivities.Activity;
import org.orcid.jaxb.model.notification.addactivities.NotificationAddActivities;
import org.orcid.jaxb.model.notification.amended.NotificationAmended;
import org.orcid.jaxb.model.notification.custom.NotificationCustom;
import org.orcid.jaxb.model.record.Education;
import org.orcid.jaxb.model.record.EducationSummary;
import org.orcid.jaxb.model.record.Employment;
import org.orcid.jaxb.model.record.Funding;
import org.orcid.jaxb.model.record.FundingContributors;
import org.orcid.jaxb.model.record.FundingSummary;
import org.orcid.jaxb.model.record.FuzzyDate;
import org.orcid.jaxb.model.record.PublicationDate;
import org.orcid.jaxb.model.record.Work;
import org.orcid.jaxb.model.record.WorkContributors;
import org.orcid.jaxb.model.record.WorkExternalIdentifier;
import org.orcid.jaxb.model.record.WorkExternalIdentifiers;
import org.orcid.persistence.jpa.entities.EndDateEntity;
import org.orcid.persistence.jpa.entities.NotificationActivityEntity;
import org.orcid.persistence.jpa.entities.NotificationAddActivitiesEntity;
import org.orcid.persistence.jpa.entities.NotificationAmendedEntity;
import org.orcid.persistence.jpa.entities.NotificationCustomEntity;
import org.orcid.persistence.jpa.entities.OrgAffiliationRelationEntity;
import org.orcid.persistence.jpa.entities.ProfileFundingEntity;
import org.orcid.persistence.jpa.entities.ProfileWorkEntity;
import org.orcid.persistence.jpa.entities.PublicationDateEntity;
import org.orcid.persistence.jpa.entities.SourceEntity;
import org.orcid.persistence.jpa.entities.StartDateEntity;
import org.orcid.persistence.jpa.entities.WorkExternalIdentifierEntity;
import org.springframework.beans.factory.FactoryBean;

/**
 * 
 * @author Will Simpson
 * 
 */
public class MapperFacadeFactory implements FactoryBean<MapperFacade> {

    @Override
    public MapperFacade getObject() throws Exception {
        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
        mapCommonFields(mapperFactory.classMap(NotificationCustomEntity.class, NotificationCustom.class)).register();
        mapCommonFields(mapperFactory.classMap(NotificationAddActivitiesEntity.class, NotificationAddActivities.class)).field("authorizationUrl", "authorizationUrl.uri")
                .field("notificationActivities", "activities.activities").register();
        mapCommonFields(mapperFactory.classMap(NotificationAmendedEntity.class, NotificationAmended.class)).register();
        mapperFactory.classMap(NotificationActivityEntity.class, Activity.class).field("externalIdType", "externalId.externalIdType")
                .field("externalIdValue", "externalId.externalIdValue").byDefault().register();
        mapperFactory.classMap(SourceEntity.class, Source.class).field("sourceClient.id", "clientId.path").byDefault().register();
        return mapperFactory.getMapperFacade();
    }

    public MapperFacade getWorkMapperFacade() {
        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();

        ConverterFactory converterFactory = mapperFactory.getConverterFactory();
        converterFactory.registerConverter("workExternalIdentifiersConverterId", new JsonOrikaConverter<WorkExternalIdentifiers>());
        converterFactory.registerConverter("workContributorsConverterId", new JsonOrikaConverter<WorkContributors>());

        ClassMapBuilder<Work, ProfileWorkEntity> classMap = mapperFactory.classMap(Work.class, ProfileWorkEntity.class);
        classMap.byDefault();
        classMap.field("putCode", "work.id");
        classMap.field("workTitle.title.content", "work.title");
        classMap.field("workTitle.translatedTitle.content", "work.translatedTitle");
        classMap.field("workTitle.translatedTitle.languageCode", "work.translatedTitleLanguageCode");
        classMap.field("shortDescription", "work.description");
        classMap.field("workCitation.workCitationType", "work.citationType");
        classMap.field("workCitation.citation", "work.citation");
        classMap.field("workType", "work.workType");
        classMap.field("publicationDate", "work.publicationDate");
        classMap.fieldMap("workExternalIdentifiers", "work.externalIdentifiersJson").converter("workExternalIdentifiersConverterId").add();
        classMap.field("url.value", "work.workUrl");
        classMap.fieldMap("workContributors", "work.contributorsJson").converter("workContributorsConverterId").add();
        classMap.field("languageCode", "work.languageCode");
        classMap.field("country.value", "work.iso2Country");        
        classMap.register();

        mapperFactory.classMap(PublicationDate.class, PublicationDateEntity.class).field("year.value", "year").field("month.value", "month").field("day.value", "day")
                .register();
        mapperFactory.classMap(WorkExternalIdentifier.class, WorkExternalIdentifierEntity.class).field("workExternalIdentifierType", "identifierType").register();
        mapperFactory.classMap(org.orcid.jaxb.model.record.Source.class, SourceEntity.class).field("sourceOrcid.path", "sourceProfile.id")
                .field("sourceClientId.path", "sourceClient.id").byDefault().register();
        return mapperFactory.getMapperFacade();
    }

    public MapperFacade getFundingMapperFacade() {
        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
        ConverterFactory converterFactory = mapperFactory.getConverterFactory();
        converterFactory.registerConverter("fundingExternalIdentifiersConverterId", new FundingExternalIdentifiersConverter());
        converterFactory.registerConverter("fundingContributorsConverterId", new JsonOrikaConverter<FundingContributors>());

        ClassMapBuilder<Funding, ProfileFundingEntity> fundingClassMap = mapperFactory.classMap(Funding.class, ProfileFundingEntity.class);
        fundingClassMap.byDefault();
        fundingClassMap.field("putCode", "id");
        fundingClassMap.field("type", "type");
        fundingClassMap.field("organizationDefinedType.content", "organizationDefinedType");
        fundingClassMap.field("title.title.content", "title");
        fundingClassMap.field("title.translatedTitle.content", "translatedTitle");
        fundingClassMap.field("title.translatedTitle.languageCode", "translatedTitleLanguageCode");
        fundingClassMap.field("description", "description");
        fundingClassMap.field("amount.content", "numericAmount");
        fundingClassMap.field("amount.currencyCode", "currencyCode");
        fundingClassMap.field("url.value", "url");        
        fundingClassMap.field("organization.name", "org.name");
        fundingClassMap.field("organization.address.city", "org.city");
        fundingClassMap.field("organization.address.region", "org.region");
        fundingClassMap.field("organization.address.country", "org.country");
        fundingClassMap.field("organization.disambiguatedOrganization.disambiguatedOrganizationIdentifier", "org.orgDisambiguated.sourceId");
        fundingClassMap.field("organization.disambiguatedOrganization.disambiguationSource", "org.orgDisambiguated.sourceType");
        fundingClassMap.fieldMap("externalIdentifiers", "externalIdentifiersJson").converter("fundingExternalIdentifiersConverterId").add();
        fundingClassMap.fieldMap("contributors", "contributorsJson").converter("fundingContributorsConverterId").add();           
        fundingClassMap.register();        
        
        ClassMapBuilder<FundingSummary, ProfileFundingEntity> fundingSummaryClassMap = mapperFactory.classMap(FundingSummary.class, ProfileFundingEntity.class);
        fundingSummaryClassMap.field("putCode", "id");
        fundingSummaryClassMap.field("type", "type");
        fundingSummaryClassMap.field("title.title.content", "title");
        fundingSummaryClassMap.field("title.translatedTitle.content", "translatedTitle");
        fundingSummaryClassMap.field("title.translatedTitle.languageCode", "translatedTitleLanguageCode");
        fundingSummaryClassMap.fieldMap("externalIdentifiers", "externalIdentifiersJson").converter("fundingExternalIdentifiersConverterId").add();
        fundingSummaryClassMap.byDefault();
        fundingSummaryClassMap.register();
        
        mapperFactory.classMap(FuzzyDate.class, StartDateEntity.class).field("year.value", "year").field("month.value", "month").field("day.value", "day").register();
        mapperFactory.classMap(FuzzyDate.class, EndDateEntity.class).field("year.value", "year").field("month.value", "month").field("day.value", "day").register();        
        mapperFactory.classMap(org.orcid.jaxb.model.record.Source.class, SourceEntity.class).field("sourceOrcid.path", "sourceClient.id").byDefault().register();
        
        return mapperFactory.getMapperFacade();
    }
    
    public MapperFacade getEducationMapperFacade() {
        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
        ClassMapBuilder<Education, OrgAffiliationRelationEntity> educationClassMap = mapperFactory.classMap(Education.class, OrgAffiliationRelationEntity.class);
        educationClassMap.byDefault();
        educationClassMap.field("putCode", "id");
        educationClassMap.field("organization.name", "org.name");
        educationClassMap.field("organization.address.city", "org.city");
        educationClassMap.field("organization.address.region", "org.region");
        educationClassMap.field("organization.address.country", "org.country");
        educationClassMap.field("organization.disambiguatedOrganization.disambiguatedOrganizationIdentifier", "org.orgDisambiguated.sourceId");
        educationClassMap.field("organization.disambiguatedOrganization.disambiguationSource", "org.orgDisambiguated.sourceType");
        educationClassMap.field("departmentName", "department");        
        educationClassMap.field("roleTitle", "title");        
        educationClassMap.register();
                        
        ClassMapBuilder<EducationSummary, OrgAffiliationRelationEntity> educationSummaryClassMap = mapperFactory.classMap(EducationSummary.class, OrgAffiliationRelationEntity.class);
        educationSummaryClassMap.field("departmentName", "department");        
        educationSummaryClassMap.field("roleTitle", "title");  
        educationSummaryClassMap.field("putCode", "id");
        educationSummaryClassMap.byDefault();
        educationSummaryClassMap.register();
        
        mapperFactory.classMap(FuzzyDate.class, StartDateEntity.class).field("year.value", "year").field("month.value", "month").field("day.value", "day").register();
        mapperFactory.classMap(FuzzyDate.class, EndDateEntity.class).field("year.value", "year").field("month.value", "month").field("day.value", "day").register();        
        mapperFactory.classMap(org.orcid.jaxb.model.record.Source.class, SourceEntity.class).field("sourceOrcid.path", "sourceClient.id").byDefault().register();
        
        return mapperFactory.getMapperFacade();
    }
    
    public MapperFacade getEmploymentMapperFacade() {
        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
        ClassMapBuilder<Employment, OrgAffiliationRelationEntity> classMap = mapperFactory.classMap(Employment.class, OrgAffiliationRelationEntity.class);
        classMap.byDefault();
        classMap.field("putCode", "id");
        classMap.field("organization.name", "org.name");
        classMap.field("organization.address.city", "org.city");
        classMap.field("organization.address.region", "org.region");
        classMap.field("organization.address.country", "org.country");
        classMap.field("organization.disambiguatedOrganization.disambiguatedOrganizationIdentifier", "org.orgDisambiguated.sourceId");
        classMap.field("organization.disambiguatedOrganization.disambiguationSource", "org.orgDisambiguated.sourceType");
        classMap.field("departmentName", "department");
        classMap.field("roleTitle", "title");        
        classMap.register();
        
        mapperFactory.classMap(FuzzyDate.class, StartDateEntity.class).field("year.value", "year").field("month.value", "month").field("day.value", "day").register();
        mapperFactory.classMap(FuzzyDate.class, EndDateEntity.class).field("year.value", "year").field("month.value", "month").field("day.value", "day").register();        
        mapperFactory.classMap(org.orcid.jaxb.model.record.Source.class, SourceEntity.class).field("sourceOrcid.path", "sourceClient.id").byDefault().register();
        
        return mapperFactory.getMapperFacade();
    }
    
    private ClassMapBuilder<?, ?> mapCommonFields(ClassMapBuilder<?, ?> builder) {
        return builder.field("dateCreated", "createdDate").field("id", "putCode").byDefault();
    }

    @Override
    public Class<?> getObjectType() {
        return MapperFacade.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
