import SwiftUI

struct FinanceView: View {
    @EnvironmentObject var appState: AppState
    @State private var showingAddRecordSheet = false
    @State private var selectedTab = 0

    var totalIncome: Double {
        appState.financeRecords.filter { !$0.isExpense }.reduce(0) { $0 + $1.amount }
    }

    var totalExpenses: Double {
        appState.financeRecords.filter { $0.isExpense }.reduce(0) { $0 + $1.amount }
    }

    var netBalance: Double {
        totalIncome - totalExpenses
    }

    var body: some View {
        VStack(spacing: 0) {
            // Balance Summary Header Card
            VStack(spacing: 12) {
                Text("Saldo Geral do Condomínio")
                    .font(.subheadline)
                    .foregroundColor(.white.opacity(0.8))

                Text(netBalance.currencyFormatted)
                    .font(.system(size: 32, weight: .bold, design: .rounded))
                    .foregroundColor(.white)

                HStack(spacing: 24) {
                    HStack {
                        Image(systemName: "arrow.down.circle.fill")
                            .foregroundColor(.green)
                        VStack(alignment: .leading) {
                            Text("Receitas")
                                .font(.caption2)
                                .foregroundColor(.white.opacity(0.8))
                            Text(totalIncome.currencyFormatted)
                                .font(.footnote)
                                .fontWeight(.bold)
                                .foregroundColor(.white)
                        }
                    }

                    HStack {
                        Image(systemName: "arrow.up.circle.fill")
                            .foregroundColor(.red)
                        VStack(alignment: .leading) {
                            Text("Despesas")
                                .font(.caption2)
                                .foregroundColor(.white.opacity(0.8))
                            Text(totalExpenses.currencyFormatted)
                                .font(.footnote)
                                .fontWeight(.bold)
                                .foregroundColor(.white)
                        }
                    }
                }
                .padding(.top, 4)
            }
            .padding()
            .frame(maxWidth: .infinity)
            .background(LinearGradient(colors: [Color.blue, Color.indigo], startPoint: .top, endPoint: .bottom))

            Picker("Lançamentos", selection: $selectedTab) {
                Text("Todos").tag(0)
                Text("Receitas").tag(1)
                Text("Despesas").tag(2)
            }
            .pickerStyle(.segmented)
            .padding()

            List {
                ForEach(filteredRecords) { record in
                    HStack {
                        Image(systemName: record.isExpense ? "minus.circle.fill" : "plus.circle.fill")
                            .font(.title2)
                            .foregroundColor(record.isExpense ? .red : .green)

                        VStack(alignment: .leading, spacing: 4) {
                            Text(record.title)
                                .font(.headline)
                            Text("\(record.category) • \(record.status)")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }

                        Spacer()

                        Text(record.amount.currencyFormatted)
                            .font(.subheadline)
                            .fontWeight(.bold)
                            .foregroundColor(record.isExpense ? .red : .green)
                    }
                    .padding(.vertical, 4)
                }
            }
            .listStyle(.insetGrouped)
        }
        .navigationTitle("Financeiro & Contas")
        .toolbar {
            Button {
                showingAddRecordSheet = true
            } label: {
                Image(systemName: "plus")
            }
        }
        .sheet(isPresented: $showingAddRecordSheet) {
            AddFinanceRecordSheetView()
        }
    }

    private var filteredRecords: [FinanceRecord] {
        switch selectedTab {
        case 1: return appState.financeRecords.filter { !$0.isExpense }
        case 2: return appState.financeRecords.filter { $0.isExpense }
        default: return appState.financeRecords
        }
    }
}

// MARK: - Add Finance Record Sheet
struct AddFinanceRecordSheetView: View {
    @Environment(\.dismiss) var dismiss
    @EnvironmentObject var appState: AppState

    @State private var title = ""
    @State private var category = "Outros"
    @State private var amountString = ""
    @State private var isExpense = true
    @State private var status = "Pago"

    let categories = ["Taxa Condominial", "Serviços", "Manutenção", "Utilidades", "Eventos", "Outros"]

    var body: some View {
        NavigationStack {
            Form {
                Section("Tipo de Lançamento") {
                    Picker("Tipo", selection: $isExpense) {
                        Text("Despesa").tag(true)
                        Text("Receita").tag(false)
                    }
                    .pickerStyle(.segmented)
                }

                Section("Dados do Lançamento") {
                    TextField("Descrição / Título", text: $title)
                    TextField("Valor (R$)", text: $amountString)
                        .keyboardType(.decimalPad)
                    Picker("Categoria", selection: $category) {
                        ForEach(categories, id: \.self) { cat in
                            Text(cat).tag(cat)
                        }
                    }
                }
            }
            .navigationTitle("Novo Lançamento")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancelar") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Salvar") {
                        if let amount = Double(amountString.replacingOccurrences(of: ",", with: ".")), !title.isEmpty {
                            let newRecord = FinanceRecord(
                                title: title,
                                category: category,
                                amount: amount,
                                isExpense: isExpense,
                                date: Date(),
                                status: status
                            )
                            appState.financeRecords.insert(newRecord, at: 0)
                            dismiss()
                        }
                    }
                }
            }
        }
    }
}
