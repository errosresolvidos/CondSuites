import SwiftUI

struct ContractsView: View {
    @EnvironmentObject var appState: AppState

    var body: some View {
        NavigationStack {
            List {
                ForEach(appState.contracts) { contract in
                    VStack(alignment: .leading, spacing: 8) {
                        HStack {
                            Text(contract.title)
                                .font(.headline)
                            Spacer()
                            Text(contract.status)
                                .font(.caption2)
                                .fontWeight(.bold)
                                .padding(.horizontal, 6)
                                .padding(.vertical, 2)
                                .background(Color.green.opacity(0.15))
                                .foregroundColor(.green)
                                .cornerRadius(6)
                        }

                        Text("Fornecedor: \(contract.vendor)")
                            .font(.subheadline)
                            .foregroundColor(.secondary)

                        HStack {
                            VStack(alignment: .leading, spacing: 2) {
                                Text("Valor Mensal")
                                    .font(.caption2)
                                    .foregroundColor(.secondary)
                                Text(contract.monthlyValue, format: .currency(code: "BRL"))
                                    .font(.subheadline)
                                    .fontWeight(.bold)
                            }

                            Spacer()

                            VStack(alignment: .trailing, spacing: 2) {
                                Text("Vencimento")
                                    .font(.caption2)
                                    .foregroundColor(.secondary)
                                Text(contract.expirationDate.formatted(date: .numeric, time: .omitted))
                                    .font(.caption)
                                    .foregroundColor(.blue)
                            }
                        }
                    }
                    .padding(.vertical, 4)
                }
            }
            .listStyle(.insetGrouped)
            .navigationTitle("Contratos de Terceiros")
        }
    }
}
