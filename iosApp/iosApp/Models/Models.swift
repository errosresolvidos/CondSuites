import Foundation
import Combine

// MARK: - Currency Helper Extension
extension Double {
    var currencyFormatted: String {
        let formatter = NumberFormatter()
        formatter.numberStyle = .currency
        formatter.currencyCode = "BRL"
        formatter.locale = Locale(identifier: "pt_BR")
        return formatter.string(from: NSNumber(value: self)) ?? String(format: "R$ %.2f", self)
    }
}

// MARK: - User Model
struct User: Identifiable, Equatable {
    let id = UUID()
    var username: String
    var role: String // "ADMIN", "SÍNDICO", "PORTARIA", "MORADOR"
}

// MARK: - Unit Model
struct CondUnit: Identifiable {
    let id = UUID()
    var number: String
    var block: String
    var residentName: String
    var phone: String
    var status: UnitStatus
    var parkingSpaces: Int
}

enum UnitStatus: String, CaseIterable {
    case compliant = "Adimplente"
    case delinquent = "Inadimplente"
    case inAgreement = "Em Acordo"
}

// MARK: - Occurrence Model
struct Occurrence: Identifiable {
    let id = UUID()
    var title: String
    var description: String
    var unit: String
    var date: Date
    var priority: Priority
    var status: OccurrenceStatus
    var category: String
}

enum Priority: String, CaseIterable {
    case low = "Baixa"
    case medium = "Média"
    case high = "Alta"
}

enum OccurrenceStatus: String, CaseIterable {
    case open = "Aberta"
    case inProgress = "Em Andamento"
    case resolved = "Concluída"
}

// MARK: - Finance Record
struct FinanceRecord: Identifiable {
    let id = UUID()
    var title: String
    var category: String
    var amount: Double
    var isExpense: Bool
    var date: Date
    var status: String
}

// MARK: - Agreement Model
struct Agreement: Identifiable {
    let id = UUID()
    var unitNumber: String
    var originalAmount: Double
    var agreedAmount: Double
    var installments: Int
    var paidInstallments: Int
    var status: String
    var date: Date
}

// MARK: - Lawsuit Model
struct Lawsuit: Identifiable {
    let id = UUID()
    var processNumber: String
    var unitNumber: String
    var court: String
    var amount: Double
    var status: String
    var lastUpdate: Date
}

// MARK: - Elevator Model
struct Elevator: Identifiable {
    let id = UUID()
    var name: String
    var block: String
    var status: ElevatorStatus
    var lastMaintenance: Date
    var nextMaintenance: Date
    var company: String
}

enum ElevatorStatus: String, CaseIterable {
    case operational = "Operacional"
    case maintenance = "Em Manutenção"
    case stopped = "Parado / Defeito"
}

// MARK: - Contract Model
struct Contract: Identifiable {
    let id = UUID()
    var title: String
    var vendor: String
    var monthlyValue: Double
    var expirationDate: Date
    var status: String
}

// MARK: - Overtime Model
struct OvertimeRecord: Identifiable {
    let id = UUID()
    var employeeName: String
    var role: String
    var hours: Double
    var date: Date
    var reason: String
    var status: String
}

// MARK: - App State Store
class AppState: ObservableObject {
    @Published var currentUser: User? = User(username: "admin", role: "ADMIN")
    @Published var isAuthenticated: Bool = true

    @Published var units: [CondUnit] = [
        CondUnit(number: "101", block: "A", residentName: "Carlos Silva", phone: "(11) 98765-4321", status: .compliant, parkingSpaces: 1),
        CondUnit(number: "102", block: "A", residentName: "Ana Souza", phone: "(11) 97654-3210", status: .delinquent, parkingSpaces: 2),
        CondUnit(number: "201", block: "B", residentName: "Roberto Lima", phone: "(11) 96543-2109", status: .inAgreement, parkingSpaces: 1),
        CondUnit(number: "302", block: "B", residentName: "Mariana Oliveira", phone: "(11) 95432-1098", status: .compliant, parkingSpaces: 2)
    ]

    @Published var occurrences: [Occurrence] = [
        Occurrence(title: "Barulho excessivo após 22h", description: "Som alto registrado no apartamento 102", unit: "102-A", date: Date(), priority: .medium, status: .open, category: "Sossego"),
        Occurrence(title: "Vazamento na garagem B", description: "Infiltração identificada próximo à vaga 15", unit: "Área Comum", date: Date().addingTimeInterval(-86400), priority: .high, status: .inProgress, category: "Manutenção"),
        Occurrence(title: "Lâmpada queimada corredor 3º andar", description: "Troca solicitada para o bloco A", unit: "Bloco A", date: Date().addingTimeInterval(-172800), priority: .low, status: .resolved, category: "Iluminação")
    ]

    @Published var financeRecords: [FinanceRecord] = [
        FinanceRecord(title: "Taxa Condominial Setembro", category: "Receita", amount: 45000.0, isExpense: false, date: Date(), status: "Recebido"),
        FinanceRecord(title: "Manutenção de Elevadores", category: "Serviços", amount: 3200.0, isExpense: true, date: Date(), status: "Pago"),
        FinanceRecord(title: "Energia Elétrica Áreas Comuns", category: "Utilidades", amount: 4800.0, isExpense: true, date: Date(), status: "Pendente")
    ]

    @Published var agreements: [Agreement] = [
        Agreement(unitNumber: "102-A", originalAmount: 5400.0, agreedAmount: 4800.0, installments: 6, paidInstallments: 2, status: "Em Dia", date: Date()),
        Agreement(unitNumber: "201-B", originalAmount: 3200.0, agreedAmount: 3000.0, installments: 4, paidInstallments: 4, status: "Quitado", date: Date())
    ]

    @Published var lawsuits: [Lawsuit] = [
        Lawsuit(processNumber: "1002345-89.2025.8.26.0100", unitNumber: "404-A", court: "2ª Vara Cível", amount: 12500.0, status: "Citação Expedida", lastUpdate: Date())
    ]

    @Published var elevators: [Elevator] = [
        Elevator(name: "Elevador Social", block: "Bloco A", status: .operational, lastMaintenance: Date().addingTimeInterval(-15*86400), nextMaintenance: Date().addingTimeInterval(15*86400), company: "Atlas Schindler"),
        Elevator(name: "Elevador Serviço", block: "Bloco A", status: .operational, lastMaintenance: Date().addingTimeInterval(-15*86400), nextMaintenance: Date().addingTimeInterval(15*86400), company: "Atlas Schindler"),
        Elevator(name: "Elevador Social", block: "Bloco B", status: .maintenance, lastMaintenance: Date(), nextMaintenance: Date().addingTimeInterval(30*86400), company: "Otis")
    ]

    @Published var contracts: [Contract] = [
        Contract(title: "Portaria Remota", vendor: "SegurMax LTDA", monthlyValue: 8500.0, expirationDate: Date().addingTimeInterval(180*86400), status: "Vigente"),
        Contract(title: "Jardinagem e Limpeza", vendor: "Verde Clean", monthlyValue: 4200.0, expirationDate: Date().addingTimeInterval(60*86400), status: "Vigente")
    ]

    @Published var overtimeRecords: [OvertimeRecord] = [
        OvertimeRecord(employeeName: "João Pereira", role: "Porteiro", hours: 3.5, date: Date(), reason: "Cobertura de turno especial", status: "Aprovado"),
        OvertimeRecord(employeeName: "Maria Santos", role: "Zeladora", hours: 2.0, date: Date().addingTimeInterval(-86400), reason: "Limpeza pós-evento salão de festas", status: "Pendente")
    ]

    func login(username: String, role: String) {
        self.currentUser = User(username: username, role: role)
        self.isAuthenticated = true
    }

    func logout() {
        self.currentUser = nil
        self.isAuthenticated = false
    }
}
