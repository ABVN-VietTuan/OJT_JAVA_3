using {sap.capire.employee as db} from '../db/eply-model';

service AdminService @(path: 'admin') {
    @restrict: [
        {
            grant: ['READ'],
            to   : 'Viewer'
        },
        {
            grant: [
                'READ',
                'CREATE',
                'UPDATE',
                'DELETE'
            ],
            to   : 'Admin'
        }
    ]
    entity Roles       as projection on db.Roles;

    @restrict: [
        {
            grant: ['READ'],
            to   : 'Viewer'
        },
        {
            grant: [
                'READ',
                'CREATE',
                'UPDATE',
                'DELETE'
            ],
            to   : 'Admin'
        }
    ]
    entity Departments as projection on db.Departments;

    @restrict: [
        {
            grant: ['READ'],
            to   : 'Viewer'
        },
        {
            grant: [
                'READ',
                'CREATE',
                'UPDATE',
                'DELETE'
            ],
            to   : 'Admin'
        }
    ]
    entity Employees   as projection on db.Employees;

    action calculateSalary(employeeId : UUID) returns Double;
}

annotate AdminService with @(requires: [
    'Admin',
    'Viewer'
]);
