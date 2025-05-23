import { DepartmentResponse } from "./departmentResponse";

export interface ObjectListResponse<T> {
    objects: T[];
    total: number;
}