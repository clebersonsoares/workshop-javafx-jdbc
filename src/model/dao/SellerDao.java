package model.dao;

import java.util.List;

import model.entities.Department;
import model.entities.Seller;

public interface SellerDao {
	void insert(Seller obj);
	
	Seller findById(Integer id);
	void update(Seller obj);

	void deleteById(Integer id);

	List<Seller> findAll();
	List<Seller>findByDepartment(Department dep);
}
