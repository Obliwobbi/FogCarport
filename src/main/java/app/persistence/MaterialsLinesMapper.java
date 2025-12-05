package app.persistence;

import app.entities.Material;
import app.entities.MaterialsLine;
import app.exceptions.DatabaseException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MaterialsLinesMapper
{
    private final ConnectionPool connectionPool;
    private final MaterialMapper materialMapper;

    public MaterialsLinesMapper(ConnectionPool connectionPool)
    {
        this.connectionPool = connectionPool;
        this.materialMapper = new MaterialMapper(connectionPool);
    }

    public void createMaterialLine(int orderId, MaterialsLine line) throws DatabaseException
    {
        String sql = """
                INSERT INTO materials_lines
                (order_id, material_id, material_name, unit_type, quantity, unit_price, line_price)
                VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING line_id
                """;

        Material material = line.getMaterial();
        if (material == null)
        {
            throw new DatabaseException("Material må ikke være null ved oprettelse af MaterialsLine");
        }

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, orderId);
            ps.setInt(2, material.getId());
            ps.setString(3, material.getName());
            ps.setString(4, material.getUnitType());
            ps.setInt(5, line.getQuantity());
            ps.setDouble(6, material.getPrice());
            ps.setDouble(7, line.getLinePrice());

            try (ResultSet rs = ps.executeQuery())
            {
                if (rs.next())
                {
                    line.setLineId(rs.getInt("line_id"));
                }
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved oprettelse af materials_line: " + e.getMessage());
        }
    }

    public List<MaterialsLine> getMaterialLinesByOrderId(int orderId) throws DatabaseException
    {
        List<MaterialsLine> lines = new ArrayList<>();

        String sql = """
                SELECT *
                FROM materials_lines
                WHERE order_id = ?
                """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery())
            {
                while (rs.next())
                {
                    int lineId = rs.getInt("line_id");
                    int quantity = rs.getInt("quantity");
                    double linePrice = rs.getDouble("line_price");
                    int materialId = rs.getInt("material_id");

                    Material material = materialMapper.getMaterialById(materialId);

                    MaterialsLine line = new MaterialsLine(
                            lineId,
                            quantity,
                            linePrice,
                            material
                            );

                    lines.add(line);
                }
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved hentning af materials_lines: " + e.getMessage());
        }
        return lines;
    }

    public boolean updateMaterialLineName(int orderId, MaterialsLine line, String newMaterialName) throws DatabaseException
    {
        String sql = """
                UPDATE materials_lines
                SET material_name=?
                WHERE order_id=? AND line_id=?;
                """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setString(1, newMaterialName);
            ps.setInt(2, orderId);
            ps.setInt(3, line.getLineId());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 1)
            {

                return true;
            } else
            {
                throw new DatabaseException("Ingen material line fundet med id: " + line.getLineId());
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved hentning af materials_lines: " + e.getMessage());
        }
    }

    public String getMaterialLineName(int orderId, MaterialsLine line) throws DatabaseException
    {
        String result = "";
        String sql = """
                SELECT material_name
                FROM materials_lines WHERE  order_id=? AND line_id=?;
                """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, orderId);
            ps.setInt(2, line.getLineId());
            try (ResultSet rs = ps.executeQuery())
            {
                if (rs.next())
                {
                    result = rs.getString("material_name");
                }
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved hentning af materials_lines: " + e.getMessage());
        }
        return result;
    }

    public boolean deleteMaterialLine(MaterialsLine line) throws DatabaseException
    {
        boolean result = false;
        String sql = """
                DELETE FROM materials_lines
                WHERE line_id=?;
                """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, line.getLineId());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 1)
            {
                result = true;
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException(e.getMessage() + "Fejl ved sletning af ordrelinje med id: " + line.getLineId());
        }
        return result;
    }
}