import SwiftUI

struct ReportsView: View {
    @EnvironmentObject var appState: AppState

    var body: some View {
        List {
            Section("Relatórios Gerenciais") {
                NavigationLink(destination: ReportDetailView(title: "Relatório de Ocorrências", icon: "exclamationmark.triangle.fill", color: .red) {
                    VStack(alignment: .leading, spacing: 12) {
                        ReportMetricRow(title: "Total de Ocorrências Registradas", value: "\(appState.occurrences.count)")
                        ReportMetricRow(title: "Ocorrências Abertas", value: "\(appState.occurrences.filter { $0.status == .open }.count)")
                        ReportMetricRow(title: "Ocorrências Concluídas", value: "\(appState.occurrences.filter { $0.status == .resolved }.count)")
                    }
                }) {
                    Label("Relatório de Ocorrências", systemName: "chart.bar.doc.horizontal.fill")
                        .foregroundColor(.primary)
                }

                NavigationLink(destination: ReportDetailView(title: "Relatório de Acordos", icon: "handshake.fill", color: .green) {
                    VStack(alignment: .leading, spacing: 12) {
                        ReportMetricRow(title: "Total de Acordos Ativos", value: "\(appState.agreements.count)")
                        ReportMetricRow(title: "Valor Total Renegociado", value: appState.agreements.reduce(0) { $0 + $1.agreedAmount }.currencyFormatted)
                    }
                }) {
                    Label("Relatório de Acordos", systemName: "handshake.fill")
                        .foregroundColor(.primary)
                }

                NavigationLink(destination: ReportDetailView(title: "Relatório de Ações Judiciais", icon: "gavel", color: .purple) {
                    VStack(alignment: .leading, spacing: 12) {
                        ReportMetricRow(title: "Processos em Andamento", value: "\(appState.lawsuits.count)")
                        ReportMetricRow(title: "Montante em Cobrança Judicial", value: appState.lawsuits.reduce(0) { $0 + $1.amount }.currencyFormatted)
                    }
                }) {
                    Label("Relatório de Ajuizados", systemName: "gavel")
                        .foregroundColor(.primary)
                }

                NavigationLink(destination: ReportDetailView(title: "Relatório de Elevadores", icon: "arrow.up.and.down.square.fill", color: .blue) {
                    VStack(alignment: .leading, spacing: 12) {
                        ReportMetricRow(title: "Elevadores Operacionais", value: "\(appState.elevators.filter { $0.status == .operational }.count)/\(appState.elevators.count)")
                        ReportMetricRow(title: "Manutenções Pendentes", value: "\(appState.elevators.filter { $0.status != .operational }.count)")
                    }
                }) {
                    Label("Relatório de Elevadores", systemName: "arrow.up.and.down.square.fill")
                        .foregroundColor(.primary)
                }

                NavigationLink(destination: ReportDetailView(title: "Relatório de Horas Extras", icon: "clock.fill", color: .orange) {
                    VStack(alignment: .leading, spacing: 12) {
                        ReportMetricRow(title: "Total de Horas Lançadas", value: String(format: "%.1f hrs", appState.overtimeRecords.reduce(0) { $0 + $1.hours }))
                        ReportMetricRow(title: "Registros Pendentes de Aprovação", value: "\(appState.overtimeRecords.filter { $0.status == "Pendente" }.count)")
                    }
                }) {
                    Label("Relatório de Horas Extras", systemName: "clock.fill")
                        .foregroundColor(.primary)
                }
            }
        }
        .listStyle(.insetGrouped)
        .navigationTitle("Relatórios & Análises")
    }
}

// MARK: - Generic Report Detail View
struct ReportDetailView<Content: View>: View {
    let title: String
    let icon: String
    let color: Color
    let content: Content

    init(title: String, icon: String, color: Color, @ViewBuilder content: () -> Content) {
        self.title = title
        self.icon = icon
        self.color = color
        self.content = content()
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                HStack(spacing: 16) {
                    Image(systemName: icon)
                        .font(.system(size: 36))
                        .foregroundColor(color)
                    VStack(alignment: .leading) {
                        Text(title)
                            .font(.title2)
                            .fontWeight(.bold)
                        Text("Sintético e Resumo Executivo")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }
                .padding()
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(Color.gray.opacity(0.1))
                .cornerRadius(16)

                VStack(alignment: .leading, spacing: 16) {
                    Text("Indicadores Principais")
                        .font(.headline)
                    content
                }
                .padding()
                .background(Color.white)
                .cornerRadius(16)
                .shadow(color: .black.opacity(0.04), radius: 6, x: 0, y: 2)

                Button {
                    // Export mock action
                } label: {
                    HStack {
                        Image(systemName: "square.and.arrow.up")
                        Text("Exportar Relatório em PDF/Excel")
                            .fontWeight(.bold)
                    }
                    .padding()
                    .frame(maxWidth: .infinity)
                    .background(Color.blue)
                    .foregroundColor(.white)
                    .cornerRadius(12)
                }
            }
            .padding()
        }
        .navigationTitle(title)
        .navigationBarTitleDisplayMode(.inline)
    }
}

struct ReportMetricRow: View {
    let title: String
    let value: String

    var body: some View {
        HStack {
            Text(title)
                .font(.subheadline)
                .foregroundColor(.secondary)
            Spacer()
            Text(value)
                .font(.headline)
                .fontWeight(.bold)
        }
        Divider()
    }
}
