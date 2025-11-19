package app.persistence;

import app.entities.BillOfMaterials;
import app.entities.MaterialsLine;
import app.exceptions.DatabaseException;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

public class BillOfMaterialsMapper
{
    private final ConnectionPool connectionPool;
    private final MaterialsLinesMapper materialsLinesMapper;

    public BillOfMaterialsMapper(ConnectionPool connectionPool)
    {
        this.connectionPool = connectionPool;
        this.materialsLinesMapper = new MaterialsLinesMapper(connectionPool);
    }

    public BillOfMaterials createBillOfMaterials(BillOfMaterials bom) throws DatabaseException
    {
        try (Connection connection = connectionPool.getConnection())
        {
            try
            {
                connection.setAutoCommit(false);

                String sql = """
                        INSERT INTO bills_of_materials (total_price)
                        VALUES (?) RETURNING bom_id, created_date
                        """;

                try (PreparedStatement ps = connection.prepareStatement(sql))
                {
                    ps.setDouble(1, bom.getTotalPrice());

                    try (ResultSet rs = ps.executeQuery())
                    {
                        if (rs.next())
                        {
                            int bomId = rs.getInt("bom_id");
                            Timestamp ts = rs.getTimestamp("created_date");
                            LocalDateTime createdDate = ts.toLocalDateTime();

                            bom.setBomId(bomId);
                            bom.setCreatedDate(createdDate);
                        }
                    }
                }
                List<MaterialsLine> lines = bom.getMaterialLines();
                if (lines != null)
                {
                    for (MaterialsLine line : lines)
                    {
                        materialsLinesMapper.createMaterialLine(bom.getBomId(), line);
                    }
                }
                connection.commit();
            }
            catch (SQLException e)
            {
                throw new DatabaseException("Fejl ved oprettelse af BillOfMaterials: " + e.getMessage());
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Database forbindelsesfejl: " + e.getMessage());
        }
        return bom;
    }


    public BillOfMaterials getBillOfMaterialsById(int bomId) throws DatabaseException
    {
        String sql = "SELECT * FROM bills_of_materials WHERE bom_id = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, bomId);

            try (ResultSet rs = ps.executeQuery())
            {
                if (rs.next())
                {
                    Timestamp ts = rs.getTimestamp("created_date");
                    LocalDateTime createdDate = ts.toLocalDateTime();
                    double totalPrice = rs.getDouble("total_price");

                    BillOfMaterials bom = new BillOfMaterials(bomId, createdDate, totalPrice);

                    bom.setMaterialLines(materialsLinesMapper.getMaterialLinesByBomId(bomId));

                    return bom;
                }
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved hentning af BillOfMaterials: " + e.getMessage());
        }
        return null;
    }
}
