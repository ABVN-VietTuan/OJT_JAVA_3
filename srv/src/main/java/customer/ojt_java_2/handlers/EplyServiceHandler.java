package customer.ojt_java_2.handlers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sap.cds.Result;
import com.sap.cds.Row;
import com.sap.cds.ql.Select;
import com.sap.cds.services.EventContext;
import com.sap.cds.services.ServiceException;
import com.sap.cds.services.authentication.JwtTokenAuthenticationInfo;
import com.sap.cds.services.cds.CqnService;
import com.sap.cds.services.handler.EventHandler;
import com.sap.cds.services.handler.annotations.After;
import com.sap.cds.services.handler.annotations.Before;
import com.sap.cds.services.handler.annotations.On;
import com.sap.cds.services.handler.annotations.ServiceName;
import com.sap.cds.services.persistence.PersistenceService;

import cds.gen.adminservice.AdminService_;
import cds.gen.adminservice.CalculateSalaryContext;
import cds.gen.adminservice.Employees_;
import cds.gen.adminservice.Roles_;

@Component
@ServiceName(AdminService_.CDS_NAME)
public class EplyServiceHandler implements EventHandler {
    @Autowired
    private final PersistenceService db;

    public EplyServiceHandler(PersistenceService db) {
        this.db = db;
    }

    @Before(event = CqnService.EVENT_READ, entity = Employees_.CDS_NAME)
    public void getmployee(EventContext context) {
        var authInfo = context.getAuthenticationInfo();

        if (authInfo != null && authInfo instanceof JwtTokenAuthenticationInfo jwtInfo) {
            String token = jwtInfo.getToken();
            System.out.println("User JWT token: " + token);
        } else {
            System.out.println("No JWT token found or authentication info is missing.");
        }

        // Also print user name or ID if available
        if (context.getUserInfo() != null) {
            System.out.println("User ID: " + context.getUserInfo().getName());
        }
    }

    @On(event = CalculateSalaryContext.CDS_NAME)
    public void calculateSalary(CalculateSalaryContext ctx) {
        var employeeId = ctx.getEmployeeId();
        // 🔹 Fetch employee hireDate + role ID
        Result empResult = db.run(
                Select.from(Employees_.CDS_NAME)
                        .columns("hireDate", "role_ID")
                        .where(e -> e.get("ID").eq(employeeId)));

        Row empRow = empResult.first().orElse(null);
        var hireDate = empRow.get("hireDate");
        var roleId = empRow.get("role_ID");

        // 🔹 Fetch base salary from Roles
        Result roleResult = db.run(
                Select.from(Roles_.CDS_NAME)
                        .columns("baseSalary")
                        .where(r -> r.get("ID").eq(roleId)));

        Row roleRow = roleResult.first().orElse(null);

        double baseSalary = (double) roleRow.get("baseSalary");

        // 🔹 Calculate bonus
        int hireYear = ((LocalDate) hireDate).getYear();
        int currentYear = LocalDate.now(ZoneId.systemDefault()).getYear();
        int yearsOfService = currentYear - hireYear;

        double bonus = yearsOfService * 1000;
        double totalSalary = baseSalary + bonus;

        ctx.setResult(totalSalary);
    }
}
