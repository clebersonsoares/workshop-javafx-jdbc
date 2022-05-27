package model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import db.DB;
import db.DbException;
import model.dao.SellerDao;
import model.entities.Department;
import model.entities.Seller;

public class SellerDaoJDBC implements SellerDao {
	private Connection con;

	public SellerDaoJDBC(Connection conn) {
		this.con = conn;
	}

	@Override
	public void insert(Seller obj) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement(
					"INSERT INTO seller(Name,Email,BirthDate,BaseSalary,DepartmentId) VALUES(?,?,?,?,?)",
					Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, obj.getName());
			ps.setString(2, obj.getEmail());
			ps.setDate(3, new java.sql.Date(obj.getBirthDate().getTime()));
			ps.setDouble(4, 5600.0);
			ps.setInt(5, obj.getDepartment().getId());
			int rowAffect = ps.executeUpdate();
			if (rowAffect > 0) {
				rs = ps.getGeneratedKeys();
				while (rs.next()) {
					int id = rs.getInt(1);
					obj.setId(id);
					System.out.println("Seller com id = " + obj.getId() + " inserido com sucesso!");
				}

			} else {
				throw new DbException("Erro ao inserir um Seller");
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} finally {
			DB.closeStatement(ps);
			DB.closeResultSet(rs);

		}

	}

	@Override
	public void update(Seller obj) {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(
					"UPDATE seller SET Name = ?,Email = ?,BirthDate = ?,BaseSalary = ?,DepartmentId = ? WHERE Id = ?");
			ps.setString(1, obj.getName());
			ps.setString(2, obj.getEmail());
			ps.setDate(3, new java.sql.Date(obj.getBirthDate().getTime()));
			ps.setDouble(4, obj.getBaseSalary());
			ps.setInt(5, obj.getDepartment().getId());
			ps.setInt(6, obj.getId());
			int rowsAffected = ps.executeUpdate();
			if (rowsAffected > 0) {
				System.out.println("Registro atualizado com sucesso!");
			}

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} finally {
			DB.closeStatement(ps);
		}
	}

	@Override
	public void deleteById(Integer id) {
		PreparedStatement ps = null;
		try {
			con.setAutoCommit(false);
			ps = con.prepareStatement("DELETE FROM seller WHERE Id = ?");
			ps.setInt(1, id);
			int rowsAffect = ps.executeUpdate();
			if(rowsAffect > 0) {
				con.commit();
				System.out.println("Seller deletado com sucesso!");
			}else {
				con.rollback();
				System.out.println("Nenhum Seller encontrado com este id!");
			}
			
		}catch(SQLException e) {
			
			System.out.println(e.getMessage());
			
		}finally {
			DB.closeStatement(ps);
		}

	}

	@Override
	public List<Seller> findAll() {
		Statement st = null;
		ResultSet rs = null;
		try {
			st = con.createStatement();
			rs = st.executeQuery(
					"SELECT seller .*,department.Name as DepName FROM seller INNER JOIN department ON seller.DepartmentId = department.Id ORDER BY Name");
			List<Seller> list = new ArrayList<Seller>();
			Map<Integer, Department> map = new HashMap<Integer, Department>();
			while (rs.next()) {
				Department dep = map.get(rs.getInt("DepartmentId"));
				if (dep == null) {
					dep = instaciateDepartment(rs);
					map.put(rs.getInt("DepartmentId"), dep);
				}
				Seller s = instanciateSeller(rs, dep);
				list.add(s);
			}
			return list;
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return null;
	}

	@Override
	public Seller findById(Integer id) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement(
					"SELECT seller.*,department.Name as DepName FROM seller INNER JOIN department ON seller.DepartmentId = department.Id WHERE seller.Id = ?");
			ps.setInt(1, id);
			rs = ps.executeQuery();
			if (rs.next()) {
				Department dep = instaciateDepartment(rs);
				Seller seller = instanciateSeller(rs, dep);
				return seller;
			}

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} finally {
			DB.closeResultSet(rs);
			DB.closeStatement(ps);
		}
		return null;

	}

	private Seller instanciateSeller(ResultSet rs, Department dep) throws SQLException {
		Seller seller = new Seller();
		seller.setId(rs.getInt("Id"));
		seller.setName(rs.getString("Name"));
		seller.setEmail(rs.getString("Email"));
		seller.setBirthDate(rs.getDate("BirthDate"));
		seller.setBaseSalary(rs.getDouble("BaseSalary"));
		seller.setDepartment(dep);
		return seller;
	}

	private Department instaciateDepartment(ResultSet rs) throws SQLException {
		Department dep = new Department();
		dep.setId(rs.getInt("DepartmentId"));
		dep.setName(rs.getString("DepName"));
		return dep;
	}

	@Override
	public List<Seller> findByDepartment(Department department) {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = con.prepareStatement(
					" SELECT seller .*,department.Name as DepName FROM seller INNER JOIN department ON seller.DepartmentId = department.Id WHERE Department.Id = ? ORDER BY Name");

			st.setInt(1, department.getId());

			rs = st.executeQuery();

			List<Seller> list = new ArrayList<>();
			Map<Integer, Department> map = new HashMap<>();

			while (rs.next()) {

				Department dep = map.get(rs.getInt("DepartmentId"));

				if (dep == null) {
					dep = instaciateDepartment(rs);
					map.put(rs.getInt("DepartmentId"), dep);
				}

				Seller obj = instanciateSeller(rs, dep);
				list.add(obj);
			}
			return list;
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

}
