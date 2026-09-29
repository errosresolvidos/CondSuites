import SwiftUI

struct LawsuitsView: View {
    @EnvironmentObject var appState: AppState
    @State private var showingAddLawsuitSheet = false

    var body: some View {
        List {
            ForEach(appState.lawsuits) { lawsuit in
                VStack(alignment: .leading, spacing: 8) {
                    HStack {
                        Text("Processo: \(lawsuit.processNumber)")
                            .font(.caption)
                            .fontWeight(.bold)
                            .foregroundColor(.blue)
                        Spacer()
                        Text(lawsuit.status)
                            .font(.caption2)
                            .fontWeight(.bold)
                            .padding(.horizontal, 6)
                            .padding(.vertical, 2)
                            .background(Color.purple.opacity(0.12))
                            .foregroundColor(.purple)
                            .cornerRadius(6)
                    }

                    HStack {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Unidade \(lawsuit.unitNumber)")
                                .font(.headline)
                            Text(lawsuit.court)
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                        Spacer()
                        VStack(alignment: .trailing, spacing: 2) {
                            Text("Valor da Causa")
                                .font(.caption2)
                                .foregroundColor(.secondary)
                            Text(lawsuit.amount.currencyFormatted)
                                .font(.subheadline)
                                .fontWeight(.bold)
                                .foregroundColor(.red)
                        }
                    }
                }
                .padding(.vertical, 4)
            }
        }
        .listStyle(.insetGrouped)
        .navigationTitle("Ações Judiciais")
        .toolbar {
            Button {
                showingAddLawsuitSheet = true
            } label: {
                Image(systemName: "gavel")
            }
        }
        .sheet(isPresented: $showingAddLawsuitSheet) {
            AddLawsuitSheetView()
        }
    }
}

// MARK: - Add Lawsuit Sheet
struct AddLawsuitSheetView: View {
    @Environment(\.dismiss) var dismiss
    @EnvironmentObject var appState: AppState

    @State private var processNumber = ""
    @State private var unitNumber = ""
    @State private var court = "2ª Vara Cível"
    @State private var amountString = ""
    @State private var status = "Peticião Inicial"

    var body: some View {
        NavigationStack {
            Form {
                Section("Dados do Processo") {
                    TextField("Número do Processo", text: $processNumber)
                    TextField("Unidade Devedora", text: $unitNumber)
                    TextField("Vara / Comarca", text: $court)
                    TextField("Valor da Causa (R$)", text: $amountString)
                        .keyboardType(.decimalPad)
                    TextField("Status Atual", text: $status)
                }
            }
            .navigationTitle("Novo Ajuizamento")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancelar") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Cadastrar") {
                        if let amt = Double(amountString.replacingOccurrences(of: ",", with: ".")), !processNumber.isEmpty {
                            let newLawsuit = Lawsuit(
                                processNumber: processNumber,
                                unitNumber: unitNumber,
                                court: court,
                                amount: amt,
                                status: status,
                                lastUpdate: Date()
                            )
                            appState.lawsuits.insert(newLawsuit, at: 0)
                            dismiss()
                        }
                    }
                }
            }
        }
    }
}
