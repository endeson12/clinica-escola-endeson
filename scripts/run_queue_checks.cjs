// Sequential browser regression runner; no installation or external publishing.
const {spawnSync}=require('node:child_process');
const path=require('node:path'),fs=require('node:fs');
const mode=process.argv[2];
if(!['--ui','--e2e'].includes(mode)) {console.error('Usage: node scripts/run_queue_checks.cjs --ui|--e2e');process.exit(2);}
const root=path.resolve(__dirname,'..');
const folder=mode==='--ui'?'queue-ui':'queue-e2e';
const prefix=mode==='--ui'?'F':'Q';
const scripts=Array.from({length:10},(_,i)=>'scripts/'+folder+'/'+prefix+String(i+1).padStart(2,'0')+'.cjs');
scripts.push('scripts/'+(mode==='--ui'?'evaluation_filter_ui_test.cjs':'evaluation_filter_e2e.cjs'));
const results=[];
for(const script of scripts) {
 const r=spawnSync(process.execPath,[script],{cwd:root,env:process.env,encoding:'utf8',timeout:90000});
 const output=(r.stdout||'')+(r.stderr||'');
 process.stdout.write(output);
 results.push({script,exit:r.status,passed:r.status===0,error:r.error?.message});
}
const report={mode,scope:mode==='--ui'?'Chromium/static assets; explicitly simulated API':'Isolated loopback JAR/PostgreSQL; HTTP and Chromium',passed:results.filter(x=>x.passed).length,total:results.length,results};
if(process.env.QA_REPORT) fs.writeFileSync(process.env.QA_REPORT,JSON.stringify(report,null,2)+'\n');
console.log(JSON.stringify(report));
if(report.passed!==report.total)process.exitCode=1;
