package com.cg.payroll.services;
import java.util.List;
import com.cg.payroll.beans.Associate;
import com.cg.payroll.beans.BankDetails;
import com.cg.payroll.beans.Salary;
import com.cg.payroll.daoservices.AssociateDAO;
import com.cg.payroll.daoservices.AssociateDAOImpl;
import com.cg.payroll.exceptions.AssociateDetailsNotFoundException;
public class PayrollServicesImpl  implements PayrollServices{
	//MOCK
	private AssociateDAO associateDao;
	public PayrollServicesImpl() {
		associateDao=new AssociateDAOImpl();
	}
	public PayrollServicesImpl(AssociateDAO associateDao) {
		super();
		this.associateDao=associateDao;
	}//
	public int acceptAssociateDetails(Associate associate) {
		associate=associateDao.save(associate);
		return associate.getAssociatedId();
	}
	@Override
	public Associate calculateNetSalary(int associateId) throws AssociateDetailsNotFoundException  {
		Associate associate=getAssociateDetails(associateId);
		Salary salary = salaryDetails(associate);
		double taxableAmount =(salary.getGrossSalary()-associate.getYearlyInvestmentUnder80C());
		salary.setMonthlyTax(calculateTax(associate, taxableAmount));
		double netSalary = (int) (taxableAmount - (salary.getMonthlyTax() + associate.getSalary().getEpf() + associate.getSalary().getCompanypf() + associate.getYearlyInvestmentUnder80C()));
		associate.getSalary().setNetSalary(netSalary);
		associate.setSalary(salaryDetails(associate));
		associateDao.update(associate);
		return associate;
	}
	
	public double calculateTax(Associate associate, double taxableAmount) {
		double taxAmount =0;
		while(taxableAmount>250000) {
			if(taxableAmount>250000&&taxableAmount<=500000) {
				taxableAmount = taxableAmount - 250000;
				taxAmount = taxAmount+taxableAmount/10;		
			}
			if(taxableAmount>500000&&taxableAmount<=1000000) {
				taxableAmount=taxableAmount-500000;
				taxAmount =taxAmount+taxableAmount/20;
			}
			if(taxableAmount>1000000) {
				taxableAmount = taxableAmount-1000000;
				taxAmount=taxAmount+taxableAmount/30;
			}
		}
		System.out.println("mponthly tax: " + taxAmount);
		return taxAmount;
	}
	
	public Salary salaryDetails(Associate associate) {
		associate.getSalary().setHra((associate.getSalary().getBasicSalary()*30/100));
		associate.getSalary().setConveyenceAllowance((associate.getSalary().getBasicSalary()*30/100));
		associate.getSalary().setOtherAllowance((associate.getSalary().getBasicSalary()*35/100));
		associate.getSalary().setPersonalAllowance(associate.getSalary().getBasicSalary()/5);
		associate.getSalary().setGrossSalary(associate.getSalary().getBasicSalary()+associate.getSalary().getConveyenceAllowance()+associate.getSalary().getHra()+associate.getSalary().getOtherAllowance()+associate.getSalary().getPersonalAllowance());
		return associate.getSalary();
	}
	
	@Override
	public Associate getAssociateDetails(int associateId) throws AssociateDetailsNotFoundException {
		Associate associate=associateDao.findOne(associateId);
		if(associate==null)
			throw new AssociateDetailsNotFoundException("Associate Details not found for Id "+associateId);
		else
			return associate;
	}
	@Override
	public List<Associate> getAllAssociateDetails() {
		return associateDao.findAll();
	}
	@Override
	public Associate calculateAnnualGrossSalary(int associateId) throws AssociateDetailsNotFoundException {
		Associate associate=getAssociateDetails(associateId);
		double grossSalary= (int) (associate.getSalary().getBasicSalary() +0.3*associate.getSalary().getBasicSalary()*2+0.25*associate.getSalary().getBasicSalary()+0.2*associate.getSalary().getBasicSalary()+associate.getSalary().getCompanypf()+associate.getSalary().getEpf())*12;
		associate.getSalary().setGrossSalary(grossSalary);
		associate.setSalary(salaryDetails(associate));
		associateDao.update(associate);
		return associate;
	}	
}