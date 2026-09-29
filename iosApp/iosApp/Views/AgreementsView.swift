import SwiftUI

struct AgreementsView: View {
    @EnvironmentObject var appState: AppState
    @State private var showingNewAgreementSheet = false

    var body: some View {
        NavigationStack {
            List {
                ForEach(appState.agreements) { agreement in
                    VStack(alignment: .leading, spacing: 10) {
                        HStack {
                            Text("Unidade \(agreement.unitNumber)")
                                .font(.headline)
                            Spacer()
                            Text(agreement.status)
                                .font(.caption2)
                                .fontWeight(.bold)
                                .padding(.horizontal, 8)
                                .padding(.vertical, 4)
                                .background(agreement.status == "Quitado" ? Color.green.opacity(0.15) : Color.blue.opacity(0.15))
                                .foregroundColor(agreement.status == "Quitado" ? .green : .blue)
                                .cornerRadius(8)
                        }

                        HStack(spacing: 16) {
                            VStack(alignment: .leading, spacing: 2) {
                                Text("Valor Original")
                                    .font(.caption2)
                                    .foregroundColor(.secondary)
                                Text(agreement.originalAmount.currencyFormatted)
                                    .font(.subheadline)
                                    .strikethrough()
                                    .foregroundColor(.secondary)
                            }

                            VStack(alignment: .leading, spacing: 2) {
                                Text("Valor Acordado")
                                    .font(.caption2)
                                    .foregroundColor(.secondary)
                                Text(agreement.agreedAmount.currencyFormatted)
                                    .font(.subheadline)
                                    .fontWeight(.bold)
                                    .foregroundColor(.green)
                            }
                        }

                        // Progress Bar
                        VStack(alignment: .leading, spacing: 4) {
                            HStack {
                                Text("Parcelas: \(agreement.paidInstallments) de \(agreement.installments)")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                                Spacer()
                                Text("\(Int(Double(agreement.paidInstallments) / Double(agreement.installments) * 100))%")
                                    .font(.caption)
                                    .fontWeight(.bold)
                            }
                            ProgressView(value: Double(agreement.paidInstallments), total: Double(agreement.installments))
                                .tint(.blue)
                        }
                    }
                    .padding(.vertical, 6)
                }
            }
            .listStyle(.insetGrouped)
            .navigationTitle("Acordos de Cobrança")
            .toolbar {
                Button {
                    showingNewAgreementSheet = true
                } label: {
                    Image(systemName: "handshake.fill")
                }
            }
            .sheet(isPresented: $showingNewAgreementSheet) {
                NewAgreementSheetView()
            }
        }
    }
}

// MARK: - New Agreement Sheet View
struct NewAgreementSheetView: View {
    @Environment(\.dismiss) var dismiss
    @EnvironmentObject var appState: AppState

    @State private var unitNumber = ""
    @State private var originalAmountString = ""
    @State private var discountPercentage = 10.0
    @State private var installments = 6

    var originalAmount: Double {
        Double(originalAmountString.replacingOccurrences(of: ",", with: ".")) ?? 0.0
    }

    var agreedAmount: Double {
        originalAmount * (1.0 - (discountPercentage / 100.0))
    }

    var installmentValue: Double {
        installments > 0 ? agreedAmount / Double(installments) : 0.0
    }

    var body: some View {
        NavigationStack {
            Form {
                Section("Dados do Inadimplente") {
                    TextField("Unidade (ex: 102-A)", text: $unitNumber)
                    TextField("Débito Original (R$)", text: $originalAmountString)
                        .keyboardType(.decimalPad)
                }

                Section("Simulação do Acordo") {
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Desconto Concedido: \(Int(discountPercentage))%")
                        Slider(value: $discountPercentage, in: 0...30, step: 5)
                    }

                    Stepper("Número de Parcelas: \(installments)x", value: $installments, in: 1...24)

                    HStack {
                        Text("Valor por Parcela:")
                            .fontWeight(.medium)
                        Spacer()
                        Text(installmentValue.currencyFormatted)
                            .fontWeight(.bold)
                            .foregroundColor(.blue)
                    }

                    HStack {
                        Text("Total do Acordo:")
                            .fontWeight(.bold)
                        Spacer()
                        Text(agreedAmount.currencyFormatted)
                            .fontWeight(.bold)
                            .foregroundColor(.green)
                    }
                }
            }
            .navigationTitle("Simular & Criar Acordo")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancelar") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Confirmar") {
                        if !unitNumber.isEmpty && originalAmount > 0 {
                            let newAg = Agreement(
                                unitNumber: unitNumber,
                                originalAmount: originalAmount,
                                agreedAmount: agreedAmount,
                                installments: installments,
                                paidInstallments: 0,
                                status: "Em Dia",
                                date: Date()
                            )
                            appState.agreements.insert(newAg, at: 0)
                            dismiss()
                        }
                    }
                }
            }
        }
    }
}
