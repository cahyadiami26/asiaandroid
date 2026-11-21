package com.example.data.api.model

import com.example.core.format.Formatters
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

data class EmployeeProfileData(
    val metaYear: Int,
    val profile: ProfileIdentityBlock,
    val employment: EmploymentBlock,
    val organization: OrganizationBlock,
    val manager: ProfileManagerBlock?,
    val contact: ContactBlock,
    val personal: PersonalBlock,
    val payroll: PayrollBlock,
    val leave: ProfileLeaveBlock,
    val documents: DocumentsSummaryBlock,
    val permissions: ProfilePermissionsBlock
)

data class ProfileIdentityBlock(
    val employeeCode: String,
    val fullName: String,
    val nickname: String?,
    val photoUrl: String?,
    val role: String,
    val roleLabel: String,
    val employmentStatus: String,
    val employmentStatusLabel: String
)

data class TenureBlock(
    val years: Int,
    val months: Int,
    val days: Int,
    val label: String
)

data class ContractBlock(
    val type: String?,
    val startDate: String?,
    val endDate: String?,
    val remainingDays: Int?,
    val expiringSoon: Boolean
)

data class EmploymentBlock(
    val contractType: String,
    val joinDate: String?,
    val tenure: TenureBlock,
    val contract: ContractBlock?,
    val resignDate: String?
)

data class PositionInfoBlock(
    val id: Int?,
    val name: String,
    val code: String?
)

data class OrganizationBlock(
    val businessUnit: String,
    val department: String,
    val position: PositionInfoBlock
)

data class ProfileManagerBlock(
    val id: Int?,
    val name: String,
    val email: String?,
    val position: String?,
    val department: String?,
    val isActive: Boolean,
    val statusLabel: String?
)

data class ContactBlock(
    val companyEmail: String,
    val username: String?,
    val phone: String?,
    val address: String?,
    val googleLinked: Boolean
)

data class PersonalBlock(
    val birthDate: String?,
    val age: Int?,
    val gender: String?,
    val maritalStatus: String?,
    val religion: String?
)

data class PayrollBlock(
    val basicSalary: Double?,
    val basicSalaryLabel: String,
    val taxStatus: String?,
    val npwp: String?,
    val bpjsKesehatan: String?,
    val bpjsKetenagakerjaan: String?,
    val bankName: String?,
    val bankAccountMasked: String?
)

data class ProfileLeaveSummaryBlock(
    val total: Double,
    val approvedDays: Double,
    val open: Double,
    val remaining: Double
)

data class ProfileLeaveBlock(
    val summary: ProfileLeaveSummaryBlock,
    val balances: List<LeaveBalanceDto>
)

data class DocumentsSummaryBlock(
    val total: Int,
    val verified: Int,
    val pending: Int,
    val rejected: Int,
    val expiringSoon: Int
)

data class ProfilePermissionsBlock(
    val role: String,
    val canApprove: Boolean,
    val isManager: Boolean,
    val editableFields: List<String>,
    val emailChangeRequiresHr: Boolean
)

object EmployeeProfileParser {

    fun parse(rawJson: String, requestedYear: Int? = null): EmployeeProfileData? {
        val root = JSONObject(rawJson)
        val data = root.optJSONObject("data") ?: return null
        val meta = root.optJSONObject("meta")
        val fallbackYear = requestedYear ?: Calendar.getInstance().get(Calendar.YEAR)
        val metaYear = meta?.optInt("year", fallbackYear)?.takeIf { it in 2000..2100 } ?: fallbackYear

        // 1. profile
        val pObj = data.optJSONObject("profile") ?: data
        val empCode = pObj.optStr("nik", "employee_code", "code") ?: "-"
        val fullName = pObj.optStr("full_name", "name") ?: "-"
        val nickname = pObj.optStr("nickname", "nick_name", "preferred_name", "call_name")
        val photoUrl = pObj.optStr("photo_url", "photo", "avatar_url", "avatar")
        val roleObj = pObj.optJSONObject("role")
        val role = roleObj?.optStr("code", "name", "value")
            ?: pObj.optStr("role", "role_code")
            ?: "KARYAWAN"
        val roleLabel = pObj.optStr("role_label")
            ?: roleObj?.optStr("label", "name")
            ?: role
        val statusObj = pObj.optJSONObject("status") ?: pObj.optJSONObject("employment_status")
        val empStatus = statusObj?.optStr("code", "value", "status")
            ?: pObj.optStr("employment_status", "status", "flag")
            ?: "ACTIVE"
        val empStatusLabel = pObj.optStr("employment_status_label", "status_label", "flag_label")
            ?: statusObj?.optStr("label", "name")
            ?: empStatus

        val profileBlock = ProfileIdentityBlock(
            employeeCode = empCode,
            fullName = fullName,
            nickname = nickname,
            photoUrl = photoUrl,
            role = role,
            roleLabel = roleLabel,
            employmentStatus = empStatus,
            employmentStatusLabel = empStatusLabel
        )

        // 2. employment
        val eObj = data.optJSONObject("employment") ?: JSONObject()
        val contractObj = eObj.optJSONObject("contract")
        val contractType = eObj.optStr("type", "contract_type", "employment_type", "status", "scheme")
            ?: contractObj?.optStr("type", "name")
            ?: "-"
        val joinDate = eObj.optStr("join_date", "joined_at", "start_date")
        val tenureObj = eObj.optJSONObject("tenure")
        val tYears = tenureObj?.optInt("years", 0) ?: 0
        val tMonths = tenureObj?.optInt("months", 0) ?: 0
        val tDays = tenureObj?.optInt("days", 0) ?: 0
        val tLabel = tenureObj?.optStr("label", "text", "formatted")
            ?: eObj.optStr("tenure_label")
            ?: buildTenureLabel(tYears, tMonths, tDays)
        val contractBlock = if (contractObj != null || eObj.has("remaining_days") || eObj.has("expiring_soon")) {
            val remDays = when {
                contractObj?.has("remaining_days") == true && !contractObj.isNull("remaining_days") -> contractObj.optInt("remaining_days")
                eObj.has("remaining_days") && !eObj.isNull("remaining_days") -> eObj.optInt("remaining_days")
                else -> null
            }
            val expSoon = contractObj?.optBool("expiring_soon", "is_expiring_soon")
                ?: eObj.optBool("expiring_soon", "is_expiring_soon")
                ?: (remDays != null && remDays in 0..60)
            ContractBlock(
                type = contractObj?.optStr("type", "name") ?: contractType.takeIf { it != "-" },
                startDate = contractObj?.optStr("start_date"),
                endDate = contractObj?.optStr("end_date", "expired_at", "expires_at") ?: eObj.optStr("contract_end_date"),
                remainingDays = remDays,
                expiringSoon = expSoon
            )
        } else {
            null
        }
        val resignDate = eObj.optStr("resign_date", "resigned_at", "termination_date")

        val employmentBlock = EmploymentBlock(
            contractType = contractType,
            joinDate = joinDate,
            tenure = TenureBlock(tYears, tMonths, tDays, tLabel),
            contract = contractBlock,
            resignDate = resignDate
        )

        // 3. organization
        val oObj = data.optJSONObject("organization") ?: JSONObject()
        val businessUnit = oObj.optNamedStr("business_unit") ?: "-"
        val department = oObj.optNamedStr("department") ?: "-"
        val posObj = oObj.optJSONObject("position") ?: oObj.optJSONObject("job_position") ?: oObj.optJSONObject("jabatan")
        val positionBlock = if (posObj != null) {
            PositionInfoBlock(
                id = posObj.optNullableInt("id"),
                name = posObj.optStr("name", "nama", "title") ?: "-",
                code = posObj.optStr("code", "kode")
            )
        } else {
            PositionInfoBlock(
                id = oObj.optNullableInt("position_id"),
                name = oObj.optStr("position", "position_name", "jabatan") ?: "-",
                code = oObj.optStr("position_code")
            )
        }
        val organizationBlock = OrganizationBlock(
            businessUnit = businessUnit,
            department = department,
            position = positionBlock
        )

        // 4. manager (explicitly null when unassigned)
        val mObj = if (data.has("manager") && !data.isNull("manager")) data.optJSONObject("manager") else null
        val managerBlock = if (mObj != null && (mObj.optStr("name", "full_name", "nama") != null || mObj.optStr("email") != null)) {
            val activeVal = if (mObj.has("is_active") && !mObj.isNull("is_active")) {
                mObj.optBoolean("is_active", true)
            } else if (mObj.has("active") && !mObj.isNull("active")) {
                mObj.optBoolean("active", true)
            } else {
                val st = mObj.optStr("status", "flag")
                st == null || st.equals("ACTIVE", ignoreCase = true) || st.equals("AKTIF", ignoreCase = true)
            }
            ProfileManagerBlock(
                id = mObj.optNullableInt("id"),
                name = mObj.optStr("name", "full_name", "nama") ?: "-",
                email = mObj.optStr("email"),
                position = mObj.optNamedStr("position") ?: mObj.optStr("jabatan"),
                department = mObj.optNamedStr("department") ?: mObj.optStr("departemen"),
                isActive = activeVal,
                statusLabel = mObj.optStr("status_label", "status")
            )
        } else {
            null
        }

        // 5. contact
        val cObj = data.optJSONObject("contact") ?: JSONObject()
        val contactBlock = ContactBlock(
            companyEmail = cObj.optStr("company_email", "email", "work_email") ?: "-",
            username = cObj.optStr("username"),
            phone = cObj.optStr("phone", "phone_number", "mobile", "telepon"),
            address = cObj.optStr("address", "alamat"),
            googleLinked = cObj.optBool("google_linked", "is_google_linked") ?: false
        )

        // 6. personal
        val perObj = data.optJSONObject("personal") ?: JSONObject()
        val personalBlock = PersonalBlock(
            birthDate = perObj.optStr("birth_date", "date_of_birth", "dob", "tanggal_lahir"),
            age = perObj.optNullableInt("age", "umur"),
            gender = perObj.optStr("gender_label", "gender", "jenis_kelamin"),
            maritalStatus = perObj.optStr("marital_status_label", "marital_status", "status_pernikahan"),
            religion = perObj.optStr("religion_label", "religion", "agama")
        )

        // 7. payroll
        val payObj = data.optJSONObject("payroll") ?: JSONObject()
        val rawBasicSalary = when {
            payObj.has("basic_salary") && !payObj.isNull("basic_salary") -> payObj.optDouble("basic_salary", Double.NaN).takeIf { !it.isNaN() }
            payObj.has("base_salary") && !payObj.isNull("base_salary") -> payObj.optDouble("base_salary", Double.NaN).takeIf { !it.isNaN() }
            else -> null
        }
        val salaryLabel = payObj.optStr("basic_salary_label", "salary_label", "formatted_basic_salary", "gaji_pokok_label")
            ?: rawBasicSalary?.let { Formatters.formatRupiah(it) }
            ?: "-"
        val bankObj = payObj.optJSONObject("bank")
        val bankName = bankObj?.optStr("name", "bank_name") ?: payObj.optStr("bank_name", "bank")
        val bankAccountMasked = bankObj?.optStr("account_number_masked", "masked_account_number", "account_number")
            ?: payObj.optStr("bank_account_masked", "account_number_masked", "masked_account_number", "bank_account_number")

        val payrollBlock = PayrollBlock(
            basicSalary = rawBasicSalary,
            basicSalaryLabel = salaryLabel,
            taxStatus = payObj.optStr("tax_status", "ptkp", "status_pajak"),
            npwp = payObj.optStr("npwp"),
            bpjsKesehatan = payObj.optStr("bpjs_kesehatan", "bpjs_health", "bpjs_kes"),
            bpjsKetenagakerjaan = payObj.optStr("bpjs_ketenagakerjaan", "bpjs_employment", "bpjs_tk"),
            bankName = bankName,
            bankAccountMasked = bankAccountMasked
        )

        // 8. leave
        val lObj = data.optJSONObject("leave") ?: JSONObject()
        val lSumObj = lObj.optJSONObject("summary") ?: lObj
        val leaveSummary = ProfileLeaveSummaryBlock(
            total = lSumObj.optDoubleVal("total", "total_quota", "quota_total"),
            approvedDays = lSumObj.optDoubleVal("approved_days", "approved", "used_days"),
            open = lSumObj.optDoubleVal("open", "pending", "open_requests"),
            remaining = lSumObj.optDoubleVal("remaining", "remaining_days", "balance")
        )
        val balancesArr = lObj.optJSONArray("balances") ?: JSONArray()
        val balancesList = mutableListOf<LeaveBalanceDto>()
        for (i in 0 until balancesArr.length()) {
            val b = balancesArr.optJSONObject(i) ?: continue
            val quota = b.optDoubleVal("quota", "kuota")
            val carry = b.optDoubleVal("carry_over", "carry")
            val used = b.optDoubleVal("used", "terpakai")
            val rem = b.optDoubleVal("remaining", "sisa")
            val totalAvail = (quota + carry).coerceAtLeast(1.0)
            val progressVal = if (b.has("progress") && !b.isNull("progress")) {
                val p = b.optDouble("progress", 0.0)
                if (p > 1.0) (p / 100.0).coerceIn(0.0, 1.0) else p.coerceIn(0.0, 1.0)
            } else {
                (used / totalAvail).coerceIn(0.0, 1.0)
            }
            val typeObj = b.optJSONObject("type") ?: b.optJSONObject("leave_type")
            balancesList.add(
                LeaveBalanceDto(
                    leaveTypeId = b.optInt("leave_type_id", typeObj?.optInt("id", 0) ?: 0),
                    typeName = b.optStr("type_name", "name") ?: typeObj?.optStr("name") ?: "Cuti",
                    typeCode = b.optStr("type_code", "code") ?: typeObj?.optStr("code") ?: "",
                    year = b.optInt("year", metaYear),
                    quota = quota,
                    carryOver = carry,
                    used = used,
                    remaining = rem,
                    unlimited = b.optBool("unlimited", "is_unlimited") ?: false,
                    progress = progressVal
                )
            )
        }
        val leaveBlock = ProfileLeaveBlock(
            summary = leaveSummary,
            balances = balancesList
        )

        // 9. documents
        val dObj = data.optJSONObject("documents") ?: JSONObject()
        val documentsBlock = DocumentsSummaryBlock(
            total = dObj.optInt("total", 0),
            verified = dObj.optInt("verified", dObj.optInt("approved", 0)),
            pending = dObj.optInt("pending", dObj.optInt("unverified", dObj.optInt("belum", 0))),
            rejected = dObj.optInt("rejected", dObj.optInt("ditolak", 0)),
            expiringSoon = dObj.optInt("expiring_soon", dObj.optInt("will_expire", 0))
        )

        // 10. permissions
        val permObj = data.optJSONObject("permissions") ?: JSONObject()
        val editableArr = permObj.optJSONArray("editable_fields")
        val editableFields = mutableListOf<String>()
        if (editableArr != null) {
            for (i in 0 until editableArr.length()) {
                val field = editableArr.optString(i)?.takeIf { it.isNotBlank() && it != "null" }
                if (field != null) editableFields.add(field)
            }
        }
        val permissionsBlock = ProfilePermissionsBlock(
            role = permObj.optStr("role") ?: role,
            canApprove = permObj.optBool("can_approve") ?: false,
            isManager = permObj.optBool("is_manager") ?: false,
            editableFields = editableFields,
            emailChangeRequiresHr = permObj.optBool("email_change_requires_hr") ?: true
        )

        return EmployeeProfileData(
            metaYear = metaYear,
            profile = profileBlock,
            employment = employmentBlock,
            organization = organizationBlock,
            manager = managerBlock,
            contact = contactBlock,
            personal = personalBlock,
            payroll = payrollBlock,
            leave = leaveBlock,
            documents = documentsBlock,
            permissions = permissionsBlock
        )
    }

    private fun buildTenureLabel(years: Int, months: Int, days: Int): String {
        val parts = mutableListOf<String>()
        if (years > 0) parts.add("$years tahun")
        if (months > 0) parts.add("$months bulan")
        if (days > 0 || parts.isEmpty()) parts.add("$days hari")
        return parts.joinToString(" ")
    }

    private fun JSONObject.optStr(vararg keys: String): String? {
        for (key in keys) {
            if (has(key) && !isNull(key)) {
                val v = optString(key).trim()
                if (v.isNotEmpty() && !v.equals("null", ignoreCase = true)) {
                    return v
                }
            }
        }
        return null
    }

    private fun JSONObject.optNamedStr(key: String): String? {
        if (!has(key) || isNull(key)) return null
        val nested = optJSONObject(key)
        if (nested != null) {
            return nested.optStr("name", "nama", "label", "title")
        }
        return optStr(key, "${key}_name")
    }

    private fun JSONObject.optNullableInt(vararg keys: String): Int? {
        for (key in keys) {
            if (has(key) && !isNull(key)) {
                val v = optInt(key, Int.MIN_VALUE)
                if (v != Int.MIN_VALUE) return v
            }
        }
        return null
    }

    private fun JSONObject.optBool(vararg keys: String): Boolean? {
        for (key in keys) {
            if (has(key) && !isNull(key)) {
                val v = opt(key)
                if (v is Boolean) return v
                if (v is Number) return v.toInt() != 0
                if (v is String) {
                    if (v.equals("true", ignoreCase = true) || v == "1") return true
                    if (v.equals("false", ignoreCase = true) || v == "0") return false
                }
            }
        }
        return null
    }

    private fun JSONObject.optDoubleVal(vararg keys: String): Double {
        for (key in keys) {
            if (has(key) && !isNull(key)) {
                val d = optDouble(key, Double.NaN)
                if (!d.isNaN()) return d
            }
        }
        return 0.0
    }
}
