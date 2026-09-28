import SwiftUI

struct OvertimeView: View {
    @EnvironmentObject var appState: AppState
    @State private var showingAddOvertimeSheet = false

    var body: some View {
        NavigationStack {
            List {
                ForEach(appState.overtimeRecords) { record in
                    VStack(alignment: .leading, spacing: 8) {
                        HStack {
                            VStack(alignment: .leading, spacing: 2) {
                                Text(record.employeeName)
                                    .font(.headline)
                                Text(record.role)
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            }

                            Spacer()

                            Text("\(record.hours, specifier: "%.1f")h")
                                .font(.title3)
                                .fontWeight(.bold)
                                .foregroundColor(.purple)
                        }

                        Text("Motivo: \(record.reason)")
                            .font(.subheadline)
                            .foregroundColor(.secondary)

                        HStack {
                            Text(record.date.formatted(date: .numeric, time: .omitted))
                                .font(.caption2)
                                .foregroundColor(.secondary)

                            Spacer()

                            Text(record.status)
                                .font(.caption2)
                                .fontWeight(.bold)
                                .padding(.horizontal, 6)
                                .padding(.vertical, 2)
                                .background(record.status == "Aprovado" ? Color.green.opacity(0.15) : Color.orange.opacity(0.15))
                                .foregroundColor(record.status == "Aprovado" ? .green : .orange)
                                .cornerRadius(6)
                        }
                    }
                    .padding(.vertical, 4)
                }
            }
            .listStyle(.insetGrouped)
            .navigationTitle("Horas Extras")
            .toolbar {
                Button {
                    showingAddOvertimeSheet = true
                } label: {
                    Image(systemName: "clock.badge.plus")
                }
            }
            .sheet(isPresented: $showingAddOvertimeSheet) {
                AddOvertimeSheetView()
            }
        }
    }
}

// MARK: - Add Overtime Sheet
struct AddOvertimeSheetView: View {
    @Environment(\.dismiss) var dismiss
    @EnvironmentObject var appState: AppState

    @State private var employeeName = ""
    @State private var role = "Porteiro"
    @State private var hoursString = ""
    @State private var reason = ""

    let roles = ["Porteiro", "Zelador", "Auxiliar de Limpeza", "Manutencista", "Folguista"]

    var body: some View {
        NavigationStack {
            Form {
                Section("Funcionário") {
                    TextField("Nome do Funcionário", text: $employeeName)
                    Picker("Cargo / Função", selection: $role) {
                        ForEach(roles, id: \.self) { r in
                            Text(r).tag(r)
                        }
                    }
                }

                Section("Horas Extras") {
                    TextField("Quantidade de Horas (ex: 2.5)", text: $hoursString)
                        .keyboardType(.decimalPad)
                    TextField("Motivo / Observação", text: $reason)
                }
            }
            .navigationTitle("Lançar Hora Extra")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancelar") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Lançar") {
                        if let hrs = Double(hoursString.replacingOccurrences(of: ",", with: ".")), !employeeName.isEmpty {
                            let newRecord = OvertimeRecord(
                                employeeName: employeeName,
                                role: role,
                                hours: hrs,
                                date: Date(),
                                reason: reason.isEmpty ? "Não especificado" : reason,
                                status: "Pendente"
                            )
                            appState.overtimeRecords.insert(newRecord, at: 0)
                            dismiss()
                        }
                    }
                }
            }
        }
    }
}
