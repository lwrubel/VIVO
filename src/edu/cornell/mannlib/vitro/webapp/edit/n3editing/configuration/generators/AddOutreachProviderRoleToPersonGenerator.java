/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.DateTimeWithPrecisionVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationUtils;
import edu.cornell.mannlib.vitro.webapp.dao.jena.QueryUtils;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.XSD;
import com.hp.hpl.jena.ontology.OntModel;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.Field;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit.RdfLiteralHash;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditN3GeneratorVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.SelectListGeneratorVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.FieldVTwo;
import edu.cornell.mannlib.vitro.webapp.web.MiscWebUtils;
import edu.cornell.mannlib.vitro.webapp.search.beans.ProhibitedFromSearch;
import edu.cornell.mannlib.vitro.webapp.utils.FrontEndEditingUtils;
import edu.cornell.mannlib.vitro.webapp.utils.FrontEndEditingUtils.EditMode;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.validators.DateTimeIntervalValidation;
/**
 * Generates the edit configuration for adding a Role to a Person.  
  
  Stage one is selecting the type of the non-person thing 
  associated with the Role with the intention of reducing the 
  number of Individuals that the user has to select from.
  Stage two is selecting the non-person Individual to associate
  with the Role. 

  This is intended to create a set of statements like:

  ?person  core:hasResearchActivityRole ?newRole.
  ?newRole rdf:type core:ResearchActivityRole ;         
           roleToActivityPredicate ?someActivity .
  ?someActivity rdf:type core:ResearchActivity .
  ?someActivity rdfs:label "activity title" .
  
  
  Each subclass of the abstract two stage Generator class will have the option of overriding certain
  methods, and must always implement the following methods:
  getRoleType
  getRoleActivityTypeOptionsType
  getRoleActivityTypeObjectClassUri
  getRoleActivityTypeLiteralOptions
     
 *
 */
public class AddOutreachProviderRoleToPersonGenerator extends AddRoleToPersonTwoStageGenerator {
	
	private Log log = LogFactory.getLog(AddOutreachProviderRoleToPersonGenerator.class);
	private static String template = "addOutreachProviderRoleToPerson.ftl";
	
    //Should this be overridden
	@Override
	protected void setTemplate(EditConfigurationVTwo editConfiguration,
			VitroRequest vreq) {
		editConfiguration.setTemplate(template);
	}


    //The default activityToRolePredicate and roleToActivityPredicates are 
	//correct for this subclass so they don't need to be overwritten
	
	//role type will always be set based on particular form
	public String getRoleType(VitroRequest vreq) {
		//TODO: Get dynamic way of including vivoweb ontology
		return "http://vivoweb.org/ontology/core#OutreachProviderRole";
	}
	
	//Each subclass generator will return its own type of option here:
	//whether literal hardcoded, based on class group, or subclasses of a specific class
	//The latter two will apparently lend some kind of uri to objectClassUri ?
	public RoleActivityOptionTypes getRoleActivityTypeOptionsType(VitroRequest vreq) {
		return RoleActivityOptionTypes.HARDCODED_LITERALS;
	}
	
	//This too will depend on the specific subclass of generator
	public String getRoleActivityTypeObjectClassUri(VitroRequest vreq) {
		return null;
	}
	

	//Outreach Provider role involves hard-coded options for the "right side" of the role or activity
	protected HashMap<String, String> getRoleActivityTypeLiteralOptions(VitroRequest vreq) {
		HashMap<String, String> literalOptions = new HashMap<String, String>();
		literalOptions.put("", "Select type");
        literalOptions.put("http://vivoweb.org/ontology/core#Association", "Association");
        literalOptions.put("http://vivoweb.org/ontology/core#Center", "Center");
        literalOptions.put("http://vivoweb.org/ontology/core#ClinicalOrganization", "Clinical Organization");
        literalOptions.put("http://vivoweb.org/ontology/core#College", "College");
        literalOptions.put("http://vivoweb.org/ontology/core#Committee", "Committee");                     
        literalOptions.put("http://vivoweb.org/ontology/core#Consortium", "Consortium");
        literalOptions.put("http://vivoweb.org/ontology/core#Department", "Department");
        literalOptions.put("http://vivoweb.org/ontology/core#Division", "Division"); 
        literalOptions.put("http://purl.org/NET/c4dm/event.owl#Event", "Event"); 
        literalOptions.put("http://vivoweb.org/ontology/core#ExtensionUnit", "Extension Unit");
        literalOptions.put("http://vivoweb.org/ontology/core#Foundation", "Foundation");
        literalOptions.put("http://vivoweb.org/ontology/core#FundingOrganization", "Funding Organization");
        literalOptions.put("http://vivoweb.org/ontology/core#GovernmentAgency", "Government Agency");
        literalOptions.put("http://vivoweb.org/ontology/core#Hospital", "Hospital");
        literalOptions.put("http://vivoweb.org/ontology/core#Institute", "Institute");
        literalOptions.put("http://vivoweb.org/ontology/core#Laboratory", "Laboratory");
        literalOptions.put("http://vivoweb.org/ontology/core#Library", "Library");
        literalOptions.put("http://vivoweb.org/ontology/core#Museum", "Museum");        
        literalOptions.put("http://xmlns.com/foaf/0.1/Organization", "Organization");
        literalOptions.put("http://vivoweb.org/ontology/core#PrivateCompany", "Private Company");
        literalOptions.put("http://vivoweb.org/ontology/core#Program", "Program");
        literalOptions.put("http://vivoweb.org/ontology/core#Project", "Project");
        literalOptions.put("http://vivoweb.org/ontology/core#Publisher", "Publisher");
        literalOptions.put("http://vivoweb.org/ontology/core#ResearchOrganization", "Research Organization");
        literalOptions.put("http://vivoweb.org/ontology/core#Team", "Team");
        literalOptions.put("http://vivoweb.org/ontology/core#School", "School");
        literalOptions.put("http://vivoweb.org/ontology/core#Service","Service");
        literalOptions.put("http://vivoweb.org/ontology/core#StudentOrganization", "Student Organization");
        literalOptions.put("http://vivoweb.org/ontology/core#University", "University");
		return literalOptions;
	}

	//isShowRoleLabelField remains true for this so doesn't need to be overwritten

}
